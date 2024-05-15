package com.cloudcipher.cloudcipher_client_v2.tool.tasks;

import com.cloudcipher.cloudcipher_client_v2.utility.ConversionUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.FileUtility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.util.Map;

public class ReceiveTask extends Task<Void> {

    private final String shareId;
    private final String keyPath;

    public ReceiveTask(String shareId, String keyPath) {
        this.shareId = shareId;
        this.keyPath = keyPath;
    }

    @Override
    protected Void call() throws Exception {
        String url = "http://localhost:8080/receive/" + this.shareId;
        HttpGet get = new HttpGet(url);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(get);
            HttpEntity responseEntity = response.getEntity();

            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("Failed to receive file: " + response.getStatusLine().getReasonPhrase());
                throw new RuntimeException("Failed to receive file: " + response.getStatusLine().getReasonPhrase());
            }

            System.out.println("File received successfully");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<>() {
            });

            String filename = (String) responseMap.get("filename");
            byte[] fileBytes = ConversionUtility.byteArrayFromBase64((String) responseMap.get("fileBytes"));
            byte[] ivBytes = ConversionUtility.byteArrayFromBase64((String) responseMap.get("ivBytes"));

            File keyFile = new File(this.keyPath);
            int[][] key = CryptoUtility.readSymmetricKey(keyFile);

            byte[] decryptedFileBytes = CryptoUtility.decrypt(fileBytes, key, ivBytes);
            FileUtility.saveDownload(decryptedFileBytes, filename);

            return null;
        }
    }
}
