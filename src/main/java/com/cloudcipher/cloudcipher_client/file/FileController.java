package com.cloudcipher.cloudcipher_client.file;

import com.cloudcipher.cloudcipher_client.CloudCipherClient;
import com.cloudcipher.cloudcipher_client.Globals;
import com.cloudcipher.cloudcipher_client.HomeController;
import com.cloudcipher.cloudcipher_client.component.FileDialog;
import com.cloudcipher.cloudcipher_client.file.model.DownloadResponse;
import com.cloudcipher.cloudcipher_client.file.model.ShareResponse;
import com.cloudcipher.cloudcipher_client.file.tasks.DeleteTask;
import com.cloudcipher.cloudcipher_client.file.tasks.DownloadTask;
import com.cloudcipher.cloudcipher_client.file.tasks.ListTask;
import com.cloudcipher.cloudcipher_client.file.tasks.ShareCloudTask;
import com.cloudcipher.cloudcipher_client.file.view.InitialDirectoryDialog;
import com.cloudcipher.cloudcipher_client.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;

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
        fileLabel.setPrefWidth(Double.MAX_VALUE);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem downloadItem = getDownloadItem(filename, fileLabel, contextMenu);
        MenuItem deleteItem = getDeleteItem(filename, fileLabel, contextMenu);
        MenuItem shareItem = getShareItem(filename, (int) size, fileLabel, contextMenu);

        contextMenu.getItems().add(downloadItem);
        if (!filename.contains("shared/")) {
            contextMenu.getItems().addAll(deleteItem, shareItem);
        }
        fileLabel.setContextMenu(contextMenu);
        fileLabel.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                contextMenu.show(fileLabel, event.getScreenX(), event.getScreenY());
            }
        });

        Label fileSize = new Label(FileUtility.parseFileSize(size));
        fileSize.setPrefWidth(Double.MAX_VALUE);

        int rowIndex = fileListGrid.getRowCount();
        fileListGrid.add(fileLabel, 0, rowIndex);
        fileListGrid.add(fileSize, 1, rowIndex);

        // Styling
        String defaultStyle = "-fx-border-color: #d6d6da; -fx-border-width: 0 0 1 0; -fx-padding: 8;";
        fileLabel.setStyle(defaultStyle);
        fileSize.setStyle(defaultStyle);

        String hoverStyle = "-fx-background-color: #f0f0f0;";
        fileLabel.setOnMouseEntered(event -> {
            fileLabel.setStyle(hoverStyle + defaultStyle);
            fileSize.setStyle(hoverStyle + defaultStyle);
        });
        fileLabel.setOnMouseExited(event -> {
            fileLabel.setStyle(defaultStyle);
            fileSize.setStyle(defaultStyle);
        });
        fileSize.setOnMouseEntered(event -> {
            fileLabel.setStyle(hoverStyle + defaultStyle);
            fileSize.setStyle(hoverStyle + defaultStyle);
        });
        fileSize.setOnMouseExited(event -> {
            fileLabel.setStyle(defaultStyle);
            fileSize.setStyle(defaultStyle);
        });
    }

    private MenuItem getShareItem(String filename, int fileSize, Label fileLabel, ContextMenu contextMenu) {
        AtomicLong startTime = new AtomicLong();
        AtomicLong endTime = new AtomicLong();

        MenuItem shareItem = new MenuItem("Share");
        shareItem.setOnAction(event -> {
            Task<ShareResponse> shareTask = new ShareCloudTask(Globals.getUsername(), Globals.getToken(), filename, fileSize);
            shareTask.setOnSucceeded(event2 -> {
                ShareResponse response = shareTask.getValue();
                try {
                    byte[] reencryptedBytes = response.getFileBytes();
                    byte[] ivBytes = response.getIvBytes();
                    String shareId = response.getShareId();
                    int[][] newKey = response.getNewKey();

                    String directory = Globals.getDefaultDirectory() + "/shared";
                    FileUtility.createDirectory(directory);

                    String specificDirectory = directory + "/" + shareId;
                    FileUtility.createDirectory(specificDirectory);

                    String reencryptedFilename = shareId + "-" + filename;
                    FileUtility.writeFile(reencryptedBytes, specificDirectory + "/" + reencryptedFilename);

                    String ivFilename = shareId + "-" + filename.split("\\.")[0] + ".iv";
                    FileUtility.writeFile(ivBytes, specificDirectory + "/" + ivFilename);

                    String keyFileName = shareId + "-" + filename.split("\\.")[0] + ".key";
                    FileUtility.writeKeyFile(newKey, specificDirectory + "/" + keyFileName);

                    fileLabel.setGraphic(null);

                    Label copySuccess = new Label("Copied to clipboard");
                    copySuccess.setStyle("-fx-text-fill: green;");
                    copySuccess.setVisible(false);

                    Label shareLinkLabel = new Label("Share ID (click to copy):\n" + shareId);
                    shareLinkLabel.setOnMouseClicked(e3 -> {
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(shareId);
                        clipboard.setContent(content);

                        copySuccess.setVisible(true);
                    });

                    endTime.set(System.nanoTime());
                    System.out.println("E2E Share Time: " + (endTime.get() - startTime.get()) + " ns");

                    Label textLabel = new Label(filename + " has been shared. Would you like to open the directory?");
                    FileDialog dialog = new FileDialog("Share ID for " + filename, specificDirectory, new VBox(shareLinkLabel, copySuccess, textLabel));
                    dialog.showAndWait();

                } catch (Exception e) {
                    Label exclamation = new Label("!");
                    exclamation.setStyle("-fx-text-fill: red;");
                    fileLabel.setGraphic(exclamation);
                }
                fileLabel.setContextMenu(contextMenu);
                fileLabel.setOnMouseClicked(event3 -> {
                    if (event3.getButton() == MouseButton.PRIMARY) {
                        contextMenu.show(fileLabel, event3.getScreenX(), event3.getScreenY());
                    }
                });
            });

            shareTask.setOnFailed(event2 -> {
                Label exclamation = new Label("!");
                exclamation.setStyle("-fx-text-fill: red;");
                fileLabel.setGraphic(exclamation);
                fileLabel.setContextMenu(contextMenu);
                fileLabel.setOnMouseClicked(event3 -> {
                    if (event3.getButton() == MouseButton.PRIMARY) {
                        contextMenu.show(fileLabel, event3.getScreenX(), event3.getScreenY());
                    }
                });
            });

            ProgressIndicator shareSpinner = new ProgressIndicator();
            shareSpinner.setVisible(true);
            shareSpinner.setManaged(true);
            shareSpinner.setProgress(-1);
            shareSpinner.setPrefSize(15, 15);

            fileLabel.setGraphic(shareSpinner);
            fileLabel.setGraphicTextGap(5);
            fileLabel.setContextMenu(null);
            fileLabel.setOnMouseClicked(null);

            Thread shareThread = new Thread(shareTask);
            shareThread.start();
            startTime.set(System.nanoTime());
        });
        return shareItem;
    }

    private MenuItem getDeleteItem(String filename, Label fileLabel, ContextMenu contextMenu) {
        AtomicLong startTime = new AtomicLong();
        AtomicLong endTime = new AtomicLong();

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            Task<Void> deleteTask = new DeleteTask(Globals.getUsername(), Globals.getToken(), filename);
            deleteTask.setOnSucceeded(event2 -> {
                refreshList();
                fileLabel.setGraphic(null);
                fileLabel.setContextMenu(contextMenu);
                fileLabel.setOnMouseClicked(event3 -> {
                    if (event3.getButton() == MouseButton.PRIMARY) {
                        contextMenu.show(fileLabel, event3.getScreenX(), event3.getScreenY());
                    }
                });
                endTime.set(System.nanoTime());
                System.out.println("E2E Delete Time: " + (endTime.get() - startTime.get()) + " ns");
            });
            deleteTask.setOnFailed(event2 -> {
                Label exclamation = new Label("!");
                exclamation.setStyle("-fx-text-fill: red;");
                fileLabel.setGraphic(exclamation);
                fileLabel.setContextMenu(contextMenu);
                fileLabel.setOnMouseClicked(event3 -> {
                    if (event3.getButton() == MouseButton.PRIMARY) {
                        contextMenu.show(fileLabel, event3.getScreenX(), event3.getScreenY());
                    }
                });
            });

            ProgressIndicator deleteSpinner = new ProgressIndicator();
            deleteSpinner.setVisible(true);
            deleteSpinner.setManaged(true);
            deleteSpinner.setProgress(-1);
            deleteSpinner.setPrefSize(15, 15);

            fileLabel.setGraphic(deleteSpinner);
            fileLabel.setGraphicTextGap(5);
            fileLabel.setContextMenu(null);
            fileLabel.setOnMouseClicked(null);

            Thread deleteThread = new Thread(deleteTask);
            deleteThread.start();
            startTime.set(System.nanoTime());
        });
        return deleteItem;
    }

    private static MenuItem getDownloadItem(String filename, Label fileLabel, ContextMenu contextMenu) {
        AtomicLong startTime = new AtomicLong();
        AtomicLong endTime = new AtomicLong();

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

                    long startDecryptTime = System.nanoTime();
                    byte[] fileBytes = CryptoUtility.decrypt(encryptedBytes, key, ivBytes);
                    long endDecryptTime = System.nanoTime();
                    System.out.println("Decrypt Time: " + (endDecryptTime - startDecryptTime) + " ns");

                    FileUtility.saveDownload(fileBytes, filename);
                    endTime.set(System.nanoTime());
                    System.out.println("E2E Download Time: " + (endTime.get() - startTime.get()) + " ns");

                    fileLabel.setGraphic(null);

                    FileDialog dialog = new FileDialog("Downloaded " + filename, Globals.getDefaultDirectory() + "/downloaded", filename + " has been downloaded to your default directory. Would you like to open the directory?");
                    dialog.showAndWait();

                } catch (Exception e) {
                    Label exclamation = new Label("!");
                    exclamation.setStyle("-fx-text-fill: red;");
                    fileLabel.setGraphic(exclamation);
                }
                fileLabel.setContextMenu(contextMenu);
                fileLabel.setOnMouseClicked(event3 -> {
                    if (event3.getButton() == MouseButton.PRIMARY) {
                        contextMenu.show(fileLabel, event3.getScreenX(), event3.getScreenY());
                    }
                });
            });

            downloadTask.setOnFailed(event2 -> {
                Label exclamation = new Label("!");
                exclamation.setStyle("-fx-text-fill: red;");
                fileLabel.setGraphic(exclamation);
                fileLabel.setContextMenu(contextMenu);
                fileLabel.setOnMouseClicked(event3 -> {
                    if (event3.getButton() == MouseButton.PRIMARY) {
                        contextMenu.show(fileLabel, event3.getScreenX(), event3.getScreenY());
                    }
                });
            });

            ProgressIndicator downloadSpinner = new ProgressIndicator();
            downloadSpinner.setVisible(true);
            downloadSpinner.setManaged(true);
            downloadSpinner.setProgress(-1);
            downloadSpinner.setPrefSize(15, 15);

            fileLabel.setGraphic(downloadSpinner);
            fileLabel.setGraphicTextGap(5);
            fileLabel.setContextMenu(null);
            fileLabel.setOnMouseClicked(null);

            Thread downloadThread = new Thread(downloadTask);
            downloadThread.start();
            startTime.set(System.nanoTime());
        });
        return downloadItem;
    }

    public void logout() throws IOException {
        Globals.setUsername(null);
        Globals.setToken(null);
        Globals.setKey(null);
        FileUtility.saveConfig();
        homeController.loadLoginView();
    }

    public void handleLogoutButtonClick() throws IOException {
        logout();
    }
}
