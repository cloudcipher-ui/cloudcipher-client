package com.cloudcipher.cloudcipher_client;

import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ResourceBundle;

public class SettingController implements Initializable {

    @FXML
    private Label selectedDirectoryLabel;
    @FXML
    private Label configLabel;

    private String selectedDirectory;
    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedDirectory = Globals.getDefaultDirectory();

        directoryChooser.setInitialDirectory(new File(selectedDirectory));
        selectedDirectoryLabel.setText(selectedDirectory);

        Path path = Path.of(System.getProperty("user.home") + "/cloudcipher/");
        configLabel.setText(path.toString());
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

    public void handleConfigButtonClick() {
        FileUtility.openDirectory(System.getProperty("user.home") + "/cloudcipher/");
    }

    public void handleDebugButtonClick() {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource("debug-view.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load());
            stage.setResizable(false);
            stage.getIcons().add(new Image(Objects.requireNonNull(CloudCipherClient.class.getResourceAsStream("logo.png"))));
            stage.setTitle("Debug");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
