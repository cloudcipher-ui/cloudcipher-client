package com.cloudcipher.cloudcipher_client_v2.file;

import com.cloudcipher.cloudcipher_client_v2.Globals;
import com.cloudcipher.cloudcipher_client_v2.file.model.DownloadResponse;
import com.cloudcipher.cloudcipher_client_v2.file.tasks.DeleteTask;
import com.cloudcipher.cloudcipher_client_v2.file.tasks.DownloadTask;
import com.cloudcipher.cloudcipher_client_v2.file.tasks.ListTask;
import com.cloudcipher.cloudcipher_client_v2.file.tasks.UploadTask;
import com.cloudcipher.cloudcipher_client_v2.file.view.InitialDirectoryDialog;
import com.cloudcipher.cloudcipher_client_v2.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.FileUtility;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class FileController implements Initializable {

    @FXML
    private GridPane fileListGrid;
    @FXML
    private HBox contentPane;
    @FXML
    private VBox loadingPane;
    @FXML
    private Label loadingError;
    @FXML
    private ProgressIndicator loadingSpinner;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Label fileSizeLabel;
    @FXML
    private Button fileButton;
    @FXML
    private Button uploadButton;

    private final FileChooser fileChooser = new FileChooser();
    private String selectedFilePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshList();
        if (Globals.getDefaultDirectory() == null) {
            InitialDirectoryDialog.createAndShowInitialDirectoryDialog();
        }
        fileChooser.setInitialDirectory(new File(Globals.getDefaultDirectory()));
    }

    private void refreshList() {
        Task<List<Map<String, String>>> listTask = new ListTask(Globals.getUsername(), Globals.getToken());
        listTask.setOnSucceeded(event -> {
            List<Map<String, String>> result = listTask.getValue();
            for (Map<String, String> stringStringMap : result) {
                addFileToList(stringStringMap.get("filename"), Long.parseLong(stringStringMap.get("size")));
            }

            contentPane.setVisible(true);
            contentPane.setManaged(true);

            loadingPane.setVisible(false);
            loadingPane.setManaged(false);
        });

        listTask.setOnFailed(event -> {
            loadingError.setText(listTask.getException().getMessage());
            loadingError.setVisible(true);

            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
        });

        contentPane.setVisible(false);
        contentPane.setManaged(false);

        loadingPane.setVisible(true);
        loadingPane.setManaged(true);
        loadingError.setVisible(false);

        fileListGrid.getChildren().clear();
        Thread thread = new Thread(listTask);
        thread.start();
    }

    private void addFileToList(String filename, long size) {
        if (filename.contains("iv/")) {
            return;
        }

        Label fileLabel = new Label(filename);
        fileLabel.setStyle("-fx-background-color: white; -fx-padding: 5 0 5 8;");
        fileLabel.setPrefWidth(Double.MAX_VALUE);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem downloadItem = getDownloadItem(filename, fileLabel, contextMenu);

        MenuItem deleteItem = getDeleteItem(filename, fileLabel, contextMenu);

        MenuItem shareItem = new MenuItem("Share");
//        shareItem.setOnAction(event ->
//                ShareDialog.createAndShowShareDialog(filename, size)
//        );

        contextMenu.getItems().add(downloadItem);
        if (!filename.contains("shared/")) {
            contextMenu.getItems().addAll(deleteItem, shareItem);
        }
        fileLabel.setContextMenu(contextMenu);

        Label fileSize = new Label(FileUtility.parseFileSize(size));
        fileSize.setStyle("-fx-background-color: white; -fx-padding: 5 0 5 8;");
        fileSize.setPrefWidth(Double.MAX_VALUE);

        int rowIndex = fileListGrid.getRowCount();
        fileListGrid.add(fileLabel, 0, rowIndex);
        fileListGrid.add(fileSize, 1, rowIndex);
    }

    private MenuItem getDeleteItem(String filename, Label fileLabel, ContextMenu contextMenu) {
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            Task<Void> deleteTask = new DeleteTask(Globals.getUsername(), Globals.getToken(), filename);
            deleteTask.setOnSucceeded(event2 -> {
                refreshList();
                fileLabel.setGraphic(null);
                fileLabel.setContextMenu(contextMenu);
            });
            deleteTask.setOnFailed(event2 -> {
                Label exclamation = new Label("!");
                exclamation.setStyle("-fx-text-fill: red;");
                fileLabel.setGraphic(exclamation);
                fileLabel.setContextMenu(contextMenu);
            });

            ProgressIndicator deleteSpinner = new ProgressIndicator();
            deleteSpinner.setVisible(true);
            deleteSpinner.setManaged(true);
            deleteSpinner.setProgress(-1);
            deleteSpinner.setPrefSize(15, 15);

            fileLabel.setGraphic(deleteSpinner);
            fileLabel.setGraphicTextGap(5);
            fileLabel.setContextMenu(null);

            Thread deleteThread = new Thread(deleteTask);
            deleteThread.start();
        });
        return deleteItem;
    }

    private static MenuItem getDownloadItem(String filename, Label fileLabel, ContextMenu contextMenu) {
        MenuItem downloadItem = new MenuItem("Download");
        downloadItem.setOnAction(event -> {
            Task<DownloadResponse> downloadTask = new DownloadTask(Globals.getUsername(), Globals.getToken(), filename);
            downloadTask.setOnSucceeded(event2 -> {
                DownloadResponse response = downloadTask.getValue();
                try {
                    byte[] encryptedBytes = response.getFileBytes();
                    byte[] ivBytes = response.getIvBytes();
                    int[][] key = Globals.getKey();
                    if (filename.contains("shared/")) {
                        key = response.getKey();
                    }

                    byte[] fileBytes = CryptoUtility.decrypt(encryptedBytes, key, ivBytes);
                    FileUtility.saveDownload(fileBytes, filename);

                    fileLabel.setGraphic(null);

                } catch (Exception e) {
                    Label exclamation = new Label("!");
                    exclamation.setStyle("-fx-text-fill: red;");
                    fileLabel.setGraphic(exclamation);
                }
                fileLabel.setContextMenu(contextMenu);
            });
            downloadTask.setOnFailed(event2 -> {
                Label exclamation = new Label("!");
                exclamation.setStyle("-fx-text-fill: red;");
                fileLabel.setGraphic(exclamation);
                fileLabel.setContextMenu(contextMenu);
            });

            ProgressIndicator downloadSpinner = new ProgressIndicator();
            downloadSpinner.setVisible(true);
            downloadSpinner.setManaged(true);
            downloadSpinner.setProgress(-1);
            downloadSpinner.setPrefSize(15, 15);

            fileLabel.setGraphic(downloadSpinner);
            fileLabel.setGraphicTextGap(5);
            fileLabel.setContextMenu(null);

            Thread downloadThread = new Thread(downloadTask);
            downloadThread.start();
        });
        return downloadItem;
    }

    @FXML
    protected void handleFileButtonClick() {
        File file = fileChooser.showOpenDialog(fileButton.getScene().getWindow());
        if (file != null) {
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

            refreshList();
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
        });

        ProgressIndicator uploadSpinner = new ProgressIndicator();
        uploadSpinner.setVisible(true);
        uploadSpinner.setManaged(true);
        uploadSpinner.setProgress(-1);
        uploadSpinner.setPrefSize(15, 15);

        uploadButton.setGraphic(uploadSpinner);
        uploadButton.setGraphicTextGap(5);

        uploadButton.setDisable(true);
        uploadButton.setOpacity(1);
        uploadSpinner.setOpacity(1);

        Thread thread = new Thread(uploadTask);
        thread.start();
    }

}
