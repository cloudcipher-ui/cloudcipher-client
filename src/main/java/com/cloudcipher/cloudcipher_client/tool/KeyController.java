package com.cloudcipher.cloudcipher_client.tool;

import com.cloudcipher.cloudcipher_client.Globals;
import com.cloudcipher.cloudcipher_client.utility.CryptoUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class KeyController {

    @FXML
    private Label errorLabel;
    @FXML
    private TextField keyName;

    public void handleGenerateKeyButtonClick() {
        if (keyName.getText().isEmpty()) {
            errorLabel.setText("Please enter a key name");
            return;
        }

        try {
            CryptoUtility.generateSymmetricKey(Globals.getDefaultDirectory() + "/" + keyName.getText() + ".key");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Key Generated");
            alert.setHeaderText(null);
            alert.setContentText("Key saved in " + Globals.getDefaultDirectory());
            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Generating Key");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
