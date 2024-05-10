package com.cloudcipher.cloudcipher_client_v2.file.tasks;

import com.cloudcipher.cloudcipher_client_v2.Globals;
import com.cloudcipher.cloudcipher_client_v2.utility.ConversionUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client_v2.file.model.EncryptionResult;
import com.cloudcipher.cloudcipher_client_v2.utility.FileUtility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class UploadTask extends Task<String> {

    private final String username;
    private final String token;
    private final File file;

    public UploadTask(String username, String token, File file) {
        this.username = username;
        this.token = token;
        this.file = file;
    }

    @Override
    protected String call() {
        byte[] fileBytes = FileUtility.readFile(this.file);
        EncryptionResult result = CryptoUtility.encrypt(fileBytes, Globals.getKey());

        long[][] encryptedFile = result.getEncryptedFile();
        byte[] encryptedFileBytes = ConversionUtility.longArrayToByteArray(encryptedFile);
        byte[] iv = result.getIv();

        String SERVER_URL = "http://localhost:8080/upload";
        HttpPost post = new HttpPost(SERVER_URL);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("username", this.username)
                .addTextBody("token", this.token)
                .addBinaryBody("file", encryptedFileBytes, org.apache.http.entity.ContentType.DEFAULT_BINARY, this.file.getName())
                .addBinaryBody("iv", iv, org.apache.http.entity.ContentType.DEFAULT_BINARY, "iv")
                .build();

        post.setEntity(entity);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            CloseableHttpResponse response = client.execute(post);
            ObjectMapper mapper = new ObjectMapper();
            HttpEntity responseEntity = response.getEntity();

            if (response.getStatusLine().getStatusCode() != 200) {
                Map<String, String> error = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<>() {});
                throw new RuntimeException(error.get("message"));
            }

            if (responseEntity == null) {
                throw new RuntimeException("Internal server error. Please try again later or contact support");
            }
            return EntityUtils.toString(responseEntity);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
