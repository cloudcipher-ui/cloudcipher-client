package com.cloudcipher.cloudcipher_client_v2.tool;

import com.cloudcipher.cloudcipher_client_v2.Globals;
import com.cloudcipher.cloudcipher_client_v2.file.model.ShareResponse;
import com.cloudcipher.cloudcipher_client_v2.tool.tasks.SendTask;
import com.cloudcipher.cloudcipher_client_v2.utility.FileUtility;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

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

        Task<ShareResponse> sendTask = new SendTask(selectedFilePath, selectedKeyPath);

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

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success: File Sent");
            alert.setHeaderText(null);
            alert.setContentText("File sent successfully. Share ID: " + shareId);
            alert.showAndWait();
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
