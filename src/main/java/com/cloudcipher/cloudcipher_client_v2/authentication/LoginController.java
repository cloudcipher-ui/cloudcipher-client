package com.cloudcipher.cloudcipher_client_v2.authentication;

import com.cloudcipher.cloudcipher_client_v2.Globals;
import com.cloudcipher.cloudcipher_client_v2.HomeController;
import com.cloudcipher.cloudcipher_client_v2.authentication.model.AuthenticationResponse;
import com.cloudcipher.cloudcipher_client_v2.authentication.tasks.AuthenticationTask;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;

import java.io.IOException;


public class LoginController {

    @Setter
    private HomeController homeController;

    @FXML
    private TextField loginUsername;
    @FXML
    private PasswordField loginPassword;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;
    @FXML
    private ProgressIndicator spinner;

    @FXML
    protected void handleLoginButtonClick() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        loginButton.setDisable(true);
        loginUsername.setDisable(true);
        loginPassword.setDisable(true);

        if (loginUsername.getText().isEmpty() || loginPassword.getText().isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            loginButton.setVisible(false);
            loginButton.setManaged(false);

            spinner.setVisible(true);
            spinner.setManaged(true);

            Task<AuthenticationResponse> loginTask = getLoginTask();
            new Thread(loginTask).start();
        }

        loginButton.setDisable(false);
        loginUsername.setDisable(false);
        loginPassword.setDisable(false);
    }

    private Task<AuthenticationResponse> getLoginTask() {
        Task<AuthenticationResponse> loginTask = new AuthenticationTask(loginUsername.getText(), loginPassword.getText(), "login");
        loginTask.setOnSucceeded(event -> {
            AuthenticationResponse result = loginTask.getValue();
            Globals.setUsername(result.getUsername());
            Globals.setToken(result.getToken());
            try {
                homeController.loadFileView();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        loginTask.setOnFailed(event -> {
            errorLabel.setText(loginTask.getException().getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);

            loginButton.setVisible(true);
            loginButton.setManaged(true);

            spinner.setVisible(false);
            spinner.setManaged(false);
        });
        return loginTask;
    }

    @FXML
    protected void handleRegisterLinkClick() throws IOException {
        homeController.loadRegisterView();
    }
}
