package com.cloudcipher.cloudcipher_client_v2.file.tasks;

import javafx.concurrent.Task;
import org.apache.http.entity.mime.MultipartEntityBuilder;

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
        String url = WebUtility.getServerUrl() + "/list";
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addTextBody("username", this.username)
                .addTextBody("token", this.token);

        return WebUtility.listRequest(url, builder);
    }
}
