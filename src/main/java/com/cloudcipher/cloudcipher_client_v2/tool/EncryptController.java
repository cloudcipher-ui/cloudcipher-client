package com.cloudcipher.cloudcipher_client_v2.tool;

import com.cloudcipher.cloudcipher_client_v2.tool.tasks.EncryptTask;
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
        button.setGraphic(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));

        encryptTask.setOnSucceeded(e -> {
            button.setDisable(false);
            button.setText("Encrypt");
            button.setGraphic(null);

            String directory = encryptTask.getValue();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success: File Encrypted");
            alert.setHeaderText(null);
            alert.setContentText("Encrypted file saved to: " + directory);
            alert.showAndWait();
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
