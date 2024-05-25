package com.cloudcipher.cloudcipher_client;

import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingController implements Initializable {

    @FXML
    private Label selectedDirectoryLabel;

    private String selectedDirectory;
    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedDirectory = Globals.getDefaultDirectory();

        directoryChooser.setInitialDirectory(new File(selectedDirectory));
        selectedDirectoryLabel.setText(selectedDirectory);
    }


    public void handleChangeDirectoryButtonClick() {
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            this.selectedDirectory = selectedDirectory.getAbsolutePath();
            selectedDirectoryLabel.setText(this.selectedDirectory);

            FileUtility.saveDefaultDirectory(this.selectedDirectory);
        }
    }


    public void handleCloseButtonClick(ActionEvent event) {
        Button button = (Button) event.getSource();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }
}
