package com.cloudcipher.cloudcipher_client_v2.tool;

import com.cloudcipher.cloudcipher_client_v2.Globals;
import com.cloudcipher.cloudcipher_client_v2.utility.FileUtility;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class BaseController implements Initializable {
    protected final FileChooser fileChooser = new FileChooser();
    protected String selectedFilePath;
    protected String selectedKeyPath;
    protected String selectedIVPath;

    @FXML
    protected Label fileLabel;
    @FXML
    protected Label keyLabel;
    @FXML
    protected Label ivLabel;
    @FXML
    protected Label errorLabel;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
    }

    public void handleSelectFileButtonClick() {
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            fileChooser.setInitialDirectory(file.getParentFile());

            String fileName = file.getName();
            long fileSize = file.length();
            fileLabel.setText(fileName + " (" + FileUtility.parseFileSize(fileSize) + ")");

            if (fileSize > Globals.getMaxSize()) {
                selectedFilePath = null;
                errorLabel.setText("File size is too large. Maximum size is 1GB.");
            } else {
                selectedFilePath = file.getAbsolutePath();
            }
        } else {
            selectedFilePath = null;
            fileLabel.setText("No file selected");
        }
    }

    public void handleSelectKeyButtonClick() {
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            fileChooser.setInitialDirectory(file.getParentFile());

            String fileName = file.getName();
            long fileSize = file.length();

            keyLabel.setText(fileName);
            if (fileSize > Globals.getMaxSize()) {
                selectedKeyPath = null;
                errorLabel.setText("Key file size is too large. Maximum size is 1GB.");
            } else {
                selectedKeyPath = file.getAbsolutePath();
                keyLabel.setText(file.getName());
            }
        } else {
            selectedKeyPath = null;
            keyLabel.setText("No file selected");
        }
    }

    public void handleSelectIVButtonClick() {
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            fileChooser.setInitialDirectory(file.getParentFile());

            String fileName = file.getName();
            long fileSize = file.length();

            ivLabel.setText(fileName);
            if (fileSize > Globals.getMaxSize()) {
                selectedIVPath = null;
                errorLabel.setText("IV file size is too large. Maximum size is 1GB.");
            } else {
                selectedIVPath = file.getAbsolutePath();
                ivLabel.setText(file.getName());
            }
        } else {
            selectedIVPath = null;
            ivLabel.setText("No file selected");
        }
    }
}
