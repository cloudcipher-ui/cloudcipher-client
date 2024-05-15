package com.cloudcipher.cloudcipher_client_v2.tool;

import com.cloudcipher.cloudcipher_client_v2.Globals;
import com.cloudcipher.cloudcipher_client_v2.tool.tasks.DecryptTask;
import com.cloudcipher.cloudcipher_client_v2.tool.tasks.EncryptTask;
import com.cloudcipher.cloudcipher_client_v2.tool.tasks.ReceiveTask;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class ToolController {
    private final FileChooser fileChooser = new FileChooser();
    private final int WIDTH = 500;
    private final int HEIGHT = 300;

    @FXML
    protected void handleEncryptButtonClick() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(Globals.getPrimaryStage());

        VBox dialogVBox = new VBox();
        dialogVBox.setSpacing(8);
        dialogVBox.setPadding(new Insets(16));
        dialogVBox.setAlignment(Pos.CENTER);

        Button selectFileButton = new Button("Select File");
        Button selectKeyButton = new Button("Select Key");
        Button encryptButton = new Button("Encrypt");

        Label selectedFileLabel = new Label();
        Label selectedKeyLabel = new Label();
        Label localErrorLabel = new Label();
        localErrorLabel.setStyle("-fx-text-fill: red");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        selectFileButton.setOnAction(e -> {
            File selectedFile;
            try {
                selectedFile = fileChooser.showOpenDialog(dialogStage);
            } catch (Exception err) {
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                selectedFile = fileChooser.showOpenDialog(dialogStage);
            }

            if (selectedFile != null) {
                double fileSize = selectedFile.length();
                if (fileSize > 1024 * 1024 * 1024) {
                    localErrorLabel.setText("File size must be less than 1GB");
                    return;
                }
                selectedFileLabel.setText(selectedFile.getAbsolutePath());
                fileChooser.setInitialDirectory(selectedFile.getParentFile());
            }
        });

        selectKeyButton.setOnAction(e -> {
            File selectedKey;
            try {
                selectedKey = fileChooser.showOpenDialog(dialogStage);
            } catch (Exception err) {
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                selectedKey = fileChooser.showOpenDialog(dialogStage);
            }

            if (selectedKey != null) {
                selectedKeyLabel.setText(selectedKey.getAbsolutePath());
                fileChooser.setInitialDirectory(selectedKey.getParentFile());
            }
        });

        encryptButton.setOnAction(e -> {
            if (selectedFileLabel.getText().isEmpty() || selectedKeyLabel.getText().isEmpty()) {
                localErrorLabel.setText("Please select a file and key");
                return;
            }

            encryptButton.setDisable(true);
            encryptButton.setVisible(false);
            encryptButton.setManaged(false);

            progressIndicator.setVisible(true);
            progressIndicator.setManaged(true);

            Task<Void> encryptionTask = new EncryptTask(selectedFileLabel.getText(), selectedKeyLabel.getText());
            encryptionTask.setOnSucceeded(event -> dialogStage.close());
            encryptionTask.setOnFailed(event -> {
                localErrorLabel.setText("Failed to encrypt file");
                encryptButton.setDisable(false);
                encryptButton.setVisible(true);
                encryptButton.setManaged(true);

                progressIndicator.setVisible(false);
                progressIndicator.setManaged(false);
            });

            Thread encryptionThread = new Thread(encryptionTask);
            encryptionThread.start();
        });

        dialogVBox.getChildren().addAll(selectFileButton, selectedFileLabel, selectKeyButton, selectedKeyLabel, localErrorLabel, encryptButton, progressIndicator);

        Scene dialogScene = new Scene(dialogVBox, WIDTH, HEIGHT);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    @FXML
    protected void handleDecryptButtonClick() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(Globals.getPrimaryStage());

        VBox dialogVBox = new VBox();
        dialogVBox.setSpacing(8);
        dialogVBox.setPadding(new Insets(16));
        dialogVBox.setAlignment(Pos.CENTER);

        Button selectFileButton = new Button("Select File");
        Button selectKeyButton = new Button("Select Key");
        Button selectIvButton = new Button("Select IV");
        Button decryptButton = new Button("Decrypt");

        Label selectedFileLabel = new Label();
        Label selectedKeyLabel = new Label();
        Label selectedIvLabel = new Label();
        Label localErrorLabel = new Label();
        localErrorLabel.setStyle("-fx-text-fill: red");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        selectFileButton.setOnAction(e -> {
            File selectedFile;
            try {
                selectedFile = fileChooser.showOpenDialog(dialogStage);
            } catch (Exception err) {
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                selectedFile = fileChooser.showOpenDialog(dialogStage);
            }

            if (selectedFile != null) {
                selectedFileLabel.setText(selectedFile.getAbsolutePath());
                fileChooser.setInitialDirectory(selectedFile.getParentFile());
            }
        });

        selectKeyButton.setOnAction(e -> {
            File selectedKey;
            try {
                selectedKey = fileChooser.showOpenDialog(dialogStage);
            } catch (Exception err) {
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                selectedKey = fileChooser.showOpenDialog(dialogStage);
            }

            if (selectedKey != null) {
                selectedKeyLabel.setText(selectedKey.getAbsolutePath());
                fileChooser.setInitialDirectory(selectedKey.getParentFile());
            }
        });

        selectIvButton.setOnAction(e -> {
            File selectedIv;
            try {
                selectedIv = fileChooser.showOpenDialog(dialogStage);
            } catch (Exception err) {
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                selectedIv = fileChooser.showOpenDialog(dialogStage);
            }

            if (selectedIv != null) {
                selectedIvLabel.setText(selectedIv.getAbsolutePath());
                fileChooser.setInitialDirectory(selectedIv.getParentFile());
            }
        });

        decryptButton.setOnAction(e -> {
            if (selectedFileLabel.getText().isEmpty() || selectedKeyLabel.getText().isEmpty() || selectedIvLabel.getText().isEmpty()) {
                localErrorLabel.setText("Please select a file, IV, and key");
                return;
            }

            decryptButton.setDisable(true);
            decryptButton.setVisible(false);
            decryptButton.setManaged(false);

            progressIndicator.setVisible(true);
            progressIndicator.setManaged(true);

            Task<Void> decryptionTask = new DecryptTask(selectedFileLabel.getText(), selectedIvLabel.getText(), selectedKeyLabel.getText());
            decryptionTask.setOnSucceeded(event -> dialogStage.close());

            decryptionTask.setOnFailed(event -> {
                localErrorLabel.setText("Failed to decrypt file");
                decryptButton.setDisable(false);
                decryptButton.setVisible(true);
                decryptButton.setManaged(true);

                progressIndicator.setVisible(false);
                progressIndicator.setManaged(false);
            });

            Thread decryptionThread = new Thread(decryptionTask);
            decryptionThread.start();
        });

        dialogVBox.getChildren().addAll(selectFileButton, selectedFileLabel, selectIvButton, selectedIvLabel, selectKeyButton, selectedKeyLabel, localErrorLabel, decryptButton, progressIndicator);

        Scene dialogScene = new Scene(dialogVBox, WIDTH, HEIGHT);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    @FXML
    protected void handleReceiveButtonClick() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(Globals.getPrimaryStage());

        VBox dialogVBox = new VBox();
        dialogVBox.setSpacing(8);
        dialogVBox.setPadding(new Insets(16));
        dialogVBox.setAlignment(Pos.CENTER);

        TextField shareIdField = new TextField();
        Button selectKeyButton = new Button("Select Key");
        Button receiveButton = new Button("Receive");

        Label selectedKeyLabel = new Label();
        Label localErrorLabel = new Label();
        localErrorLabel.setStyle("-fx-text-fill: red");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        selectKeyButton.setOnAction(e -> {
            File selectedKey;
            try {
                selectedKey = fileChooser.showOpenDialog(dialogStage);
            } catch (Exception err) {
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                selectedKey = fileChooser.showOpenDialog(dialogStage);
            }

            if (selectedKey != null) {
                selectedKeyLabel.setText(selectedKey.getAbsolutePath());
                fileChooser.setInitialDirectory(selectedKey.getParentFile());
            }
        });

        receiveButton.setOnAction(e -> {
            if (shareIdField.getText().isEmpty() || selectedKeyLabel.getText().isEmpty()) {
                localErrorLabel.setText("Please enter a share ID and select a key");
                return;
            }

            receiveButton.setDisable(true);
            receiveButton.setVisible(false);
            receiveButton.setManaged(false);

            progressIndicator.setVisible(true);
            progressIndicator.setManaged(true);

            Task<Void> receiveTask = new ReceiveTask(shareIdField.getText(), selectedKeyLabel.getText());
            receiveTask.setOnSucceeded(event -> dialogStage.close());

            receiveTask.setOnFailed(event -> {
                localErrorLabel.setText("Failed to receive file");
                receiveButton.setDisable(false);
                receiveButton.setVisible(true);
                receiveButton.setManaged(true);

                progressIndicator.setVisible(false);
                progressIndicator.setManaged(false);
            });

            Thread receiveThread = new Thread(receiveTask);
            receiveThread.start();
        });

        dialogVBox.getChildren().addAll(shareIdField, selectKeyButton, selectedKeyLabel, localErrorLabel, receiveButton, progressIndicator);

        Scene dialogScene = new Scene(dialogVBox, WIDTH, HEIGHT);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }
}
