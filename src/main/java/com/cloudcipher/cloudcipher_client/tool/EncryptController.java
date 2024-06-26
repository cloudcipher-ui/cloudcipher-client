package com.cloudcipher.cloudcipher_client.tool;

import com.cloudcipher.cloudcipher_client.component.FileDialog;
import com.cloudcipher.cloudcipher_client.tool.tasks.EncryptTask;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

public class EncryptController extends BaseController {

    public void handleEncryptButtonClick(ActionEvent event) {
        if (selectedFilePath == null) {
            errorLabel.setText("Please select a file to encrypt.");
            return;
        }
        if (selectedKeyPath == null) {
            errorLabel.setText("Please select a key file.");
            return;
        }

        Task<String> encryptTask = new EncryptTask(selectedFilePath, selectedKeyPath);

        Button button = (Button) event.getSource();
        button.setDisable(true);
        button.setText("Encrypting...");

        ProgressIndicator ps = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        ps.setPrefSize(15, 15);
        button.setGraphic(ps);

        encryptTask.setOnSucceeded(e -> {
            button.setDisable(false);
            button.setText("Encrypt");
            button.setGraphic(null);

            String directory = encryptTask.getValue();
            FileDialog dialog = new FileDialog("Encrypted", directory, "Your file has been encrypted to your default directory. Would you like to open the directory?");
            dialog.showAndWait();
        });
        encryptTask.setOnFailed(e -> {
            button.setDisable(false);
            button.setText("Encrypt");
            button.setGraphic(null);
            errorLabel.setText("Failed to encrypt file.");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error: Encryption Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to encrypt file.");
            alert.setContentText(encryptTask.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(encryptTask).start();
    }
}
