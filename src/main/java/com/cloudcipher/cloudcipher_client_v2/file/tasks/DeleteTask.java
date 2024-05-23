package com.cloudcipher.cloudcipher_client_v2.file.tasks;

import com.cloudcipher.cloudcipher_client_v2.utility.WebUtility;
import javafx.concurrent.Task;


public class DeleteTask extends Task<Void> {
    private final String username;
    private final String token;
    private final String filename;

    public DeleteTask(String username, String token, String filename) {
        this.username = username;
        this.token = token;
        this.filename = filename;
    }

    @Override
    protected Void call() {
        WebUtility.deleteRequest(username, token, filename);
        return null;
    }
}
