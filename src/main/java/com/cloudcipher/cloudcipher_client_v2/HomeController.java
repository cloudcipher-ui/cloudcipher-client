package com.cloudcipher.cloudcipher_client_v2;

import com.cloudcipher.cloudcipher_client_v2.authentication.LoginController;
import com.cloudcipher.cloudcipher_client_v2.authentication.RegisterController;
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
    Pane cloudStoragePane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadLoginView();
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
        cloudStoragePane.getChildren().add(view);
    }
}
