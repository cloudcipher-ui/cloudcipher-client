package com.cloudcipher.cloudcipher_client.file;

import com.cloudcipher.cloudcipher_client.Globals;
import com.cloudcipher.cloudcipher_client.file.tasks.UploadTask;
import com.cloudcipher.cloudcipher_client.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SideFileMenuController implements Initializable {

    @FXML
    private VBox createKeyView;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Label fileSizeLabel;
    @FXML
    private Button fileButton;
    @FXML
    private Button uploadButton;


    private String selectedFilePath;
    private final FileChooser fileChooser = new FileChooser();

    @Setter
    private FileController fileController;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Globals.getKey() != null) {
            createKeyView.setVisible(false);
            createKeyView.setManaged(false);
        } else {
            fileButton.setDisable(true);
            fileButton.setVisible(false);
        }
        fileChooser.setInitialDirectory(new File(Globals.getDefaultDirectory()));
    }

    @FXML
    protected void handleFileButtonClick() {
        File file = fileChooser.showOpenDialog(fileButton.getScene().getWindow());
        if (file != null) {
            fileChooser.setInitialDirectory(file.getParentFile());

            String fileName = file.getName();
            double fileSize = file.length();
            double max_file_size = 1024 * 1024 * 1024;
            if (fileSize > max_file_size) {
                fileNameLabel.setText(fileName);
                fileNameLabel.setStyle("-fx-text-fill: red;");

                fileSizeLabel.setText(FileUtility.parseFileSize(fileSize) + " is too large");
            } else {
                selectedFilePath = file.getAbsolutePath();
                fileNameLabel.setText(fileName);
                fileNameLabel.setStyle("-fx-text-fill: black;");

                fileSizeLabel.setText(FileUtility.parseFileSize(fileSize));
                fileSizeLabel.setStyle("-fx-text-fill: black;");

                uploadButton.setDisable(false);
                uploadButton.setVisible(true);
                uploadButton.setManaged(true);
                uploadButton.setFocusTraversable(true);

                fileButton.setDisable(true);
                fileButton.setVisible(false);
                fileButton.setManaged(false);
                fileButton.setFocusTraversable(false);
            }

            fileNameLabel.setVisible(true);
            fileNameLabel.setManaged(true);

            fileSizeLabel.setVisible(true);
            fileSizeLabel.setManaged(true);
        }
    }

    @FXML
    protected void handleUploadButtonClick() {
        File file = new File(selectedFilePath);
        Task<String> uploadTask = new UploadTask(Globals.getUsername(), Globals.getToken(), file);
        uploadTask.setOnSucceeded(event -> {
            fileNameLabel.setText("File uploaded");
            fileNameLabel.setStyle("-fx-text-fill: black;");

            fileSizeLabel.setVisible(false);
            fileSizeLabel.setManaged(false);

            uploadButton.setVisible(false);
            uploadButton.setManaged(false);
            uploadButton.setFocusTraversable(false);

            fileButton.setDisable(false);
            fileButton.setVisible(true);
            fileButton.setManaged(true);
            fileButton.setFocusTraversable(true);

            uploadButton.setGraphic(null);
            uploadButton.setText("Upload");

            fileController.refreshList();
        });

        uploadTask.setOnFailed(event -> {
            fileNameLabel.setText(uploadTask.getException().getMessage());
            fileNameLabel.setStyle("-fx-text-fill: red;");

            fileSizeLabel.setVisible(false);
            fileSizeLabel.setManaged(false);

            uploadButton.setVisible(false);
            uploadButton.setFocusTraversable(false);

            fileButton.setDisable(false);
            fileButton.setVisible(true);
            fileButton.setFocusTraversable(true);

            uploadButton.setGraphic(null);
            uploadButton.setText("Upload");
        });

        ProgressIndicator uploadSpinner = new ProgressIndicator();
        uploadSpinner.setStyle("-fx-progress-color: #fff;");
        uploadSpinner.setVisible(true);
        uploadSpinner.setManaged(true);
        uploadSpinner.setProgress(-1);
        uploadSpinner.setPrefSize(20, 20);

        uploadButton.setGraphic(uploadSpinner);
        uploadButton.setGraphicTextGap(5);
        uploadButton.setText("Uploading...");

        uploadButton.setDisable(true);
        uploadButton.setOpacity(1);
        uploadSpinner.setOpacity(1);

        Thread thread = new Thread(uploadTask);
        thread.start();
    }

    @FXML
    protected void handleGenerateButtonClick() throws IOException {
        int[][] key = CryptoUtility.generateSymmetricKey(FileUtility.getApplicationPath() + "/" + Globals.getUsername() + ".key");
        Globals.setKey(key);
        FileUtility.saveConfig();

        createKeyView.setVisible(false);
        createKeyView.setManaged(false);

        fileButton.setDisable(false);
        fileButton.setVisible(true);
    }

    @FXML
    protected void handleImportButtonClick() throws IOException {
        File file = fileChooser.showOpenDialog(fileButton.getScene().getWindow());
        if (file != null) {
            int[][] key = CryptoUtility.readSymmetricKey(file);
            Globals.setKey(key);
            FileUtility.saveConfig();

            createKeyView.setVisible(false);
            createKeyView.setManaged(false);

            fileButton.setDisable(false);
            fileButton.setVisible(true);
        }
    }

    public void handleLogoutButtonClick() throws IOException {
        fileController.logout();
    }
}
