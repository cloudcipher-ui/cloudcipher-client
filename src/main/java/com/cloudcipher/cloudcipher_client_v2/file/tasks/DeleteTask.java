package com.cloudcipher.cloudcipher_client_v2.file.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

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
        String SERVER_URL = "http://localhost:8080/delete";
        HttpPost post = new HttpPost(SERVER_URL);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("username", this.username)
                .addTextBody("token", this.token)
                .addTextBody("filename", this.filename)
                .build();

        post.setEntity(entity);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();

            if (response.getStatusLine().getStatusCode() != 200) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> error = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<>() {
                });
                throw new RuntimeException(error.get("message"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file");
        }
        return null;
    }
}
