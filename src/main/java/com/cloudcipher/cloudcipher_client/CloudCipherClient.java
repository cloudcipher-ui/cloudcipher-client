package com.cloudcipher.cloudcipher_client;

import atlantafx.base.theme.CupertinoLight;
import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CloudCipherClient extends Application {
    int WIDTH = 800;
    int HEIGHT = 400;

    @Override
    public void start(Stage stage) throws IOException {
        configSetup();

        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        Globals.setPrimaryStage(stage);

        FXMLLoader fxmlLoader;
        if (Globals.getDefaultDirectory() == null) {
            fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource("onboarding-view.fxml"));
        } else {
            fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource("home-view.fxml"));
        }

        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(CloudCipherClient.class.getResourceAsStream("logo.png"))));
        stage.setTitle("CloudCipher");

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private void configSetup() {
        try {
            FileUtility.createDirectory(FileUtility.getApplicationPath());
            FileUtility.loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}