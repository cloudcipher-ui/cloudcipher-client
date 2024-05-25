package com.cloudcipher.cloudcipher_client.authentication;

import com.cloudcipher.cloudcipher_client.HomeController;
import com.cloudcipher.cloudcipher_client.authentication.model.AuthenticationResponse;
import com.cloudcipher.cloudcipher_client.authentication.tasks.AuthenticationTask;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;

import java.io.IOException;

@Setter
public class RegisterController {

    private HomeController homeController;


    @FXML
    private TextField registerUsername;
    @FXML
    private PasswordField registerPassword;
    @FXML
    private PasswordField registerConfirmPassword;
    @FXML
    private Button registerButton;
    @FXML
    private Label errorLabel;
    @FXML
    private ProgressIndicator spinner;


    @FXML
    protected void handleRegisterButtonClick() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        registerButton.setDisable(true);
        registerUsername.setDisable(true);
        registerPassword.setDisable(true);
        registerConfirmPassword.setDisable(true);

        if (registerUsername.getText().isEmpty() || registerPassword.getText().isEmpty() || registerConfirmPassword.getText().isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else if (!registerPassword.getText().equals(registerConfirmPassword.getText())) {
            errorLabel.setText("Passwords do not match");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            registerButton.setVisible(false);
            registerButton.setManaged(false);

            spinner.setVisible(true);
            spinner.setManaged(true);

            Task<AuthenticationResponse> registerTask = getRegisterTask();
            new Thread(registerTask).start();
        }

        registerButton.setDisable(false);
        registerUsername.setDisable(false);
        registerPassword.setDisable(false);
        registerConfirmPassword.setDisable(false);
    }

    private Task<AuthenticationResponse> getRegisterTask() {
        Task<AuthenticationResponse> registerTask = new AuthenticationTask(registerUsername.getText(), registerPassword.getText(), "register");

        registerTask.setOnSucceeded(event -> {
            errorLabel.setText("Registration successful");
            errorLabel.setTextFill(javafx.scene.paint.Color.GREEN);

            registerButton.setText("Continue to Login");
            registerButton.setOnAction(e -> {
                try {
                    homeController.loadLoginView();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            errorLabel.setVisible(true);
            errorLabel.setManaged(true);

            registerButton.setVisible(true);
            registerButton.setManaged(true);

            spinner.setVisible(false);
            spinner.setManaged(false);
        });

        registerTask.setOnFailed(event -> {
            errorLabel.setText(registerTask.getException().getMessage());
            errorLabel.setTextFill(javafx.scene.paint.Color.RED);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);

            registerButton.setVisible(true);
            registerButton.setManaged(true);

            spinner.setVisible(false);
            spinner.setManaged(false);
        });
        return registerTask;
    }

    public void handleLoginLinkClick() throws IOException {
        homeController.loadLoginView();
    }
}
