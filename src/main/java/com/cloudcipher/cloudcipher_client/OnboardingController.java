package com.cloudcipher.cloudcipher_client;

import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class OnboardingController {

    @FXML
    private Label selectedDirectoryLabel;
    @FXML
    private Button continueButton;

    private String selectedDirectory;
    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    public void handleSelectDirectoryButtonClick() {
        File file = directoryChooser.showDialog(Globals.getPrimaryStage());
        if (file != null) {
            directoryChooser.setInitialDirectory(file.getParentFile());

            selectedDirectory = file.getAbsolutePath();
            selectedDirectoryLabel.setText(selectedDirectory);

            continueButton.setDisable(false);
        }
    }

    public void handleContinueButtonClick() throws IOException {
        FileUtility.saveDefaultDirectory(selectedDirectory);

        Stage primaryStage =  Globals.getPrimaryStage();
        FXMLLoader fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource("home-view.fxml"));
        primaryStage.getScene().setRoot(fxmlLoader.load());
        primaryStage.show();
    }
}
