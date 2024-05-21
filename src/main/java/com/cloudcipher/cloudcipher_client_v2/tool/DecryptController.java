package com.cloudcipher.cloudcipher_client_v2.tool;

import com.cloudcipher.cloudcipher_client_v2.tool.tasks.DecryptTask;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

public class DecryptController extends BaseController {
    
    public void handleDecryptButtonClick(ActionEvent event) {
        if (selectedFilePath == null) {
            errorLabel.setText("Please select a file to decrypt.");
            return;
        }
        if (selectedKeyPath == null) {
            errorLabel.setText("Please select a key file.");
            return;
        }

        Task<String> decryptTask = new DecryptTask(selectedFilePath, selectedIVPath, selectedKeyPath);

        Button button = (Button) event.getSource();
        button.setDisable(true);
        button.setText("decrypting...");
        button.setGraphic(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));

        decryptTask.setOnSucceeded(e -> {
            button.setDisable(false);
            button.setText("Decrypt");
            button.setGraphic(null);

            String directory = decryptTask.getValue();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success: File decrypted");
            alert.setHeaderText(null);
            alert.setContentText("Decrypted file saved to: " + directory);
            alert.showAndWait();
        });
        decryptTask.setOnFailed(e -> {
            button.setDisable(false);
            button.setText("decrypt");
            button.setGraphic(null);
            errorLabel.setText("Failed to decrypt file.");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error: Decryption Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to decrypt file.");
            alert.setContentText(decryptTask.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(decryptTask).start();
    }
}
