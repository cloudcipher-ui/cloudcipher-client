package com.cloudcipher.cloudcipher_client.tool;

import com.cloudcipher.cloudcipher_client.Globals;
import com.cloudcipher.cloudcipher_client.component.FileDialog;
import com.cloudcipher.cloudcipher_client.file.model.ShareResponse;
import com.cloudcipher.cloudcipher_client.tool.tasks.ShareLocalTask;
import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

public class SendController extends BaseController {

    public void handleSendButtonClick(ActionEvent event) {
        if (selectedFilePath == null) {
            errorLabel.setText("Please select a file to send.");
            return;
        }
        if (selectedKeyPath == null) {
            errorLabel.setText("Please select a key file.");
            return;
        }

        Task<ShareResponse> sendTask = new ShareLocalTask(selectedFilePath, selectedKeyPath);

        Button button = (Button) event.getSource();
        button.setDisable(true);
        button.setText("Sending...");

        ProgressIndicator ps = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        ps.setPrefSize(15, 15);
        button.setGraphic(ps);

        sendTask.setOnSucceeded(e -> {
            button.setDisable(false);
            button.setText("Send");
            button.setGraphic(null);

            ShareResponse response = sendTask.getValue();
            byte[] reencryptedBytes = response.getFileBytes();
            byte[] ivBytes = response.getIvBytes();
            String shareId = response.getShareId();
            int[][] newKey = response.getNewKey();

            String filename = selectedFilePath.split("\\\\")[selectedFilePath.split("\\\\").length - 1];

            String directory = Globals.getDefaultDirectory() + "/shared";
            FileUtility.createDirectory(directory);

            String specificDirectory = directory + "/" + shareId;
            FileUtility.createDirectory(specificDirectory);

            String reencryptedFilename = shareId + "_" + filename;
            FileUtility.writeFile(reencryptedBytes, specificDirectory + "/" + reencryptedFilename);

            String ivFilename = shareId + "_" + filename.split("\\.")[0] + ".iv";
            FileUtility.writeFile(ivBytes, specificDirectory + "/" + ivFilename);

            String keyFileName = shareId + "_" + filename.split("\\.")[0] + ".key";
            FileUtility.writeKeyFile(newKey, specificDirectory + "/" + keyFileName);

            Label copySuccess = new Label("Copied to clipboard");
            copySuccess.setStyle("-fx-text-fill: green;");
            copySuccess.setVisible(false);

            Label shareLinkLabel = new Label("Share ID (click to copy):\n" + shareId);
            shareLinkLabel.setOnMouseClicked(e3 -> {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(shareId);
                clipboard.setContent(content);

                copySuccess.setVisible(true);
            });


            Label textLabel = new Label(filename + " has been shared. Would you like to open the directory?");
            FileDialog dialog = new FileDialog("Share ID for " + filename, specificDirectory, new VBox(shareLinkLabel, copySuccess, textLabel));
            dialog.showAndWait();
        });

        sendTask.setOnFailed(e -> {
            button.setDisable(false);
            button.setText("Send");
            button.setGraphic(null);
            errorLabel.setText("Failed to send file.");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error: Send Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to send file.");
            alert.setContentText(sendTask.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(sendTask).start();
    }
}
