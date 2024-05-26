package com.cloudcipher.cloudcipher_client.tool;

import com.cloudcipher.cloudcipher_client.Globals;
import com.cloudcipher.cloudcipher_client.component.FileDialog;
import com.cloudcipher.cloudcipher_client.tool.tasks.ReceiveTask;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class ReceiveController extends BaseController {

    @FXML
    private TextField shareID;

    public void handleReceiveButtonClick(ActionEvent event) {
        if (shareID == null) {
            errorLabel.setText("Please enter a share ID.");
            return;
        }
        if (selectedKeyPath == null) {
            errorLabel.setText("Please select a key file.");
            return;
        }

        Task<String> receiveTask = new ReceiveTask(shareID.getText(), selectedKeyPath);

        Button button = (Button) event.getSource();
        button.setDisable(true);
        button.setText("Receiving...");

        ProgressIndicator ps = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        ps.setPrefSize(15, 15);
        button.setGraphic(ps);

        receiveTask.setOnSucceeded(e -> {
            button.setDisable(false);
            button.setText("Receive");
            button.setGraphic(null);

            String directory = receiveTask.getValue();
            FileDialog dialog = new FileDialog("File Received", directory, "The file has been received to your default directory. Would you like to open the directory?");
            dialog.showAndWait();
        });
        receiveTask.setOnFailed(e -> {
            button.setDisable(false);
            button.setText("Receive");
            button.setGraphic(null);
            errorLabel.setText("Failed to receive file.");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error: Receive Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to receive file.");
            alert.setContentText(receiveTask.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(receiveTask).start();
    }
}
