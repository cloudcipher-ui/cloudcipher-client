package com.cloudcipher.cloudcipher_client_v2.authentication.tasks;

import com.cloudcipher.cloudcipher_client_v2.authentication.model.AuthenticationResponse;
import javafx.concurrent.Task;
import com.cloudcipher.cloudcipher_client_v2.utility.WebUtility;

public class AuthenticationTask extends Task<AuthenticationResponse> {

    private final String username;
    private final String password;
    private final String type;

    public AuthenticationTask(String username, String password, String type) {
        this.username = username;
        this.password = password;
        this.type = type;
    }

    @Override
    protected AuthenticationResponse call() {
        return WebUtility.authRequest(this.type, this.username, this.password);
    }
}
