package com.cloudcipher.cloudcipher_client_v2.file.tasks;

import javafx.concurrent.Task;

import com.cloudcipher.cloudcipher_client_v2.utility.WebUtility;

import java.util.List;
import java.util.Map;

public class ListTask extends Task<List<Map<String, String>>> {

    private final String username;
    private final String token;

    public ListTask(String username, String token) {
        this.username = username;
        this.token = token;
    }

    @Override
    protected List<Map<String, String>> call() {
        return WebUtility.listRequest(this.username, this.token);
    }
}
