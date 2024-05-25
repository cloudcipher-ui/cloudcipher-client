package com.cloudcipher.cloudcipher_client.file.view;

import com.cloudcipher.cloudcipher_client.Globals;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

public class InitialDirectoryDialog {

    public static void createAndShowInitialDirectoryDialog() {
        // create a dialog with one button, when the user clicks the button, open a directory chooser. if the user selects a directory, save the directory to the globals and close the dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Initial Directory");
        dialog.setHeaderText("Select the directory where you would like to store your files.");
        dialog.getDialogPane().setStyle("-fx-padding: 8px;");

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(selectButtonType);

        Button selectButton = (Button) dialog.getDialogPane().lookupButton(selectButtonType);
        selectButton.setDefaultButton(true);

        Label directoryLabel = new Label("Directory: ");
        Label directoryPath = new Label();
        dialog.getDialogPane().setContent(directoryLabel);
        dialog.getDialogPane().setContent(directoryPath);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Select Directory");
                java.io.File selectedDirectory = directoryChooser.showDialog(dialog.getOwner());
                if (selectedDirectory != null) {
                    directoryPath.setText(selectedDirectory.getAbsolutePath());
                    return selectedDirectory.getAbsolutePath();
                }
            }
            return null;
        });
        // save the directory to the globals
        dialog.showAndWait().ifPresent(Globals::setDefaultDirectory);
    }
}
