package com.cloudcipher.cloudcipher_client;

import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CloudCipherClient extends Application {
    int WIDTH = 800;
    int HEIGHT = 400;

    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());

        Globals.setPrimaryStage(stage);

        FXMLLoader fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource("home-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        stage.setResizable(false);

        stage.setTitle("CloudCipher");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}