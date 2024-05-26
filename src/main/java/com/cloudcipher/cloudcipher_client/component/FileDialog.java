package com.cloudcipher.cloudcipher_client.component;

import com.cloudcipher.cloudcipher_client.Globals;
import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class FileDialog extends Dialog<Void> {
    public FileDialog(String title, String directory, String textLabel) {
        setHeaderText(null);
        setTitle(title);
        setContentText(textLabel);

        ButtonType openButton = new ButtonType("Open", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE, openButton);
        getDialogPane().setPadding(new Insets(8));

        setResultConverter(buttonType -> {
            if (buttonType == openButton) {
                FileUtility.openDirectory(directory);
            }
            return null;
        });
    }

    public FileDialog(String title, String directory, Node child) {
        setHeaderText(null);
        setTitle(title);
        setContentText(null);
        getDialogPane().setContent(child);

        ButtonType openButton = new ButtonType("Open", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE, openButton);
        getDialogPane().setPadding(new Insets(8));

        setResultConverter(buttonType -> {
            if (buttonType == openButton) {
                FileUtility.openDirectory(directory);
            }
            return null;
        });
    }
}
