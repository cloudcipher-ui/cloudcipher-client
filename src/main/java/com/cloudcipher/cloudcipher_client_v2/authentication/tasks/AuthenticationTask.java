package com.cloudcipher.cloudcipher_client_v2.authentication.tasks;

import com.cloudcipher.cloudcipher_client_v2.authentication.model.AuthenticationResponse;
import javafx.concurrent.Task;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import utility.WebUtility;

public class AuthenticationTask extends Task<AuthenticationResponse> {

    private final String username;
    private final String password;
    private final String type;
    private final WebUtility webUtility = new WebUtility();

    public AuthenticationTask(String username, String password, String type) {
        this.username = username;
        this.password = password;
        this.type = type;
    }

    @Override
    protected AuthenticationResponse call() {
        String url = webUtility.getServerUrl() + this.type;
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addTextBody("username", this.username)
                .addTextBody("password", this.password);

        return webUtility.authRequest(url, builder);
    }
}
