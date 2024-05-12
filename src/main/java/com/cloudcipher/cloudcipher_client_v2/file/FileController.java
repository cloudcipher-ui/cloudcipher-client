package com.cloudcipher.cloudcipher_client_v2.file;

import com.cloudcipher.cloudcipher_client_v2.CloudCipherClient;
import com.cloudcipher.cloudcipher_client_v2.Globals;
import com.cloudcipher.cloudcipher_client_v2.HomeController;
import com.cloudcipher.cloudcipher_client_v2.file.model.DownloadResponse;
import com.cloudcipher.cloudcipher_client_v2.file.tasks.DeleteTask;
import com.cloudcipher.cloudcipher_client_v2.file.tasks.DownloadTask;
import com.cloudcipher.cloudcipher_client_v2.file.tasks.ListTask;
import com.cloudcipher.cloudcipher_client_v2.file.view.InitialDirectoryDialog;
import com.cloudcipher.cloudcipher_client_v2.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.FileUtility;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.io.IOException;
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
    private Button errorLogoutButton;

    @Setter
    private HomeController homeController;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXMLLoader fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource("file/side-menu-view.fxml"));
        Parent view;
        try {
            view = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SideFileMenuController sideFileMenuController = fxmlLoader.getController();
        sideFileMenuController.setFileController(this);
        contentPane.getChildren().add(0, view);

        refreshList();
        if (Globals.getDefaultDirectory() == null) {
            InitialDirectoryDialog.createAndShowInitialDirectoryDialog();
        }
    }

    void refreshList() {
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

            errorLogoutButton.setVisible(true);
            errorLogoutButton.setManaged(true);
        });

        contentPane.setVisible(false);
        contentPane.setManaged(false);

        loadingPane.setVisible(true);
        loadingPane.setManaged(true);
        loadingError.setVisible(false);
        errorLogoutButton.setVisible(false);
        errorLogoutButton.setManaged(false);

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

    public void logout() throws IOException {
        Globals.setUsername(null);
        Globals.setToken(null);
        Globals.setKey(null);
        homeController.loadLoginView();
    }

    public void handleLogoutButtonClick() throws IOException {
        logout();
    }
}
