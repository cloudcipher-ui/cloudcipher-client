package com.cloudcipher.cloudcipher_client;

import com.cloudcipher.cloudcipher_client.authentication.LoginController;
import com.cloudcipher.cloudcipher_client.authentication.RegisterController;
import com.cloudcipher.cloudcipher_client.file.FileController;
import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Pane localToolsPane;
    @FXML
    private Pane cloudStoragePane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            FileUtility.loadConfig();
            FileUtility.loadSymmetricKey();

            if (Globals.getUsername() != null && Globals.getToken() != null) {
                loadFileView();
            } else {
                loadLoginView();
            }

            loadLocalToolsView();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadLoginView() throws IOException {
        cloudStoragePane.getChildren().clear();
        FXMLLoader fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource("authentication/login-view.fxml"));
        Parent view = fxmlLoader.load();
        LoginController loginController = fxmlLoader.getController();
        loginController.setHomeController(this);
        cloudStoragePane.getChildren().add(view);
    }

    public void loadRegisterView() throws IOException {
        cloudStoragePane.getChildren().clear();
        FXMLLoader fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource("authentication/register-view.fxml"));
        Parent view = fxmlLoader.load();
        RegisterController registerController = fxmlLoader.getController();
        registerController.setHomeController(this);
        cloudStoragePane.getChildren().add(view);
    }

    public void loadFileView() throws IOException {
        cloudStoragePane.getChildren().clear();
        FXMLLoader fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource("file/file-view.fxml"));
        Parent view = fxmlLoader.load();
        FileController fileController = fxmlLoader.getController();
        fileController.setHomeController(this);
        cloudStoragePane.getChildren().add(view);
    }

    public void loadLocalToolsView() throws IOException {
        localToolsPane.getChildren().clear();
        FXMLLoader fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource("tool/tool-view.fxml"));
        Parent view = fxmlLoader.load();
        localToolsPane.getChildren().add(view);
    }
}
