package com.cloudcipher.cloudcipher_client.tool;

import com.cloudcipher.cloudcipher_client.CloudCipherClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class ToolController implements Initializable {

    @FXML
    private StackPane contentPane;
    @FXML
    private ToggleButton encryptToggleButton;
    @FXML
    private ToggleButton decryptToggleButton;
    @FXML
    private ToggleButton receiveToggleButton;
    @FXML
    private ToggleButton sendToggleButton;
    @FXML
    private ToggleButton keyToggleButton;

    private final ToggleGroup toolToggleGroup = new ToggleGroup();

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        toolToggleGroup.getToggles().addAll(encryptToggleButton, decryptToggleButton, receiveToggleButton, sendToggleButton, keyToggleButton);
        toolToggleGroup.selectedToggleProperty().addListener(
            (ov, toggle, new_toggle) -> {
                if (new_toggle == null) {
                    toggle.setSelected(true);
                } else {
                    String toggleBtn = ((ToggleButton) new_toggle).getText();
                    try {
                        String resource = "tool/" + toggleBtn.toLowerCase() + "-view.fxml";
                        loadContentView(resource);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        );

        encryptToggleButton.setSelected(true);
        try {
            loadContentView("tool/encrypt-view.fxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadContentView(String resource) throws IOException {
        contentPane.getChildren().clear();
        FXMLLoader fxmlLoader = new FXMLLoader(CloudCipherClient.class.getResource(resource));
        Parent view = fxmlLoader.load();
        contentPane.getChildren().add(view);
    }
}
