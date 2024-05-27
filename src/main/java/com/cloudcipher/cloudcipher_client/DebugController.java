package com.cloudcipher.cloudcipher_client;

import com.cloudcipher.cloudcipher_client.utility.DebugUtility;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class DebugController {
    public void handleEncryptTestClick(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        button.setDisable(true);
        DebugUtility.encryptTest();
        button.setDisable(false);
    }

    public void handleDecryptTestClick(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        button.setDisable(true);
        DebugUtility.decryptTest();
        button.setDisable(false);
    }
}
