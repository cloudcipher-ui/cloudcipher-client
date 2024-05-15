package com.cloudcipher.cloudcipher_client_v2.file.tasks;

import com.cloudcipher.cloudcipher_client_v2.Globals;
import com.cloudcipher.cloudcipher_client_v2.file.model.ShareResponse;
import com.cloudcipher.cloudcipher_client_v2.utility.ConversionUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.RG;
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

import java.util.Map;

public class ShareTask extends Task<ShareResponse> {

    private final String username;
    private final String token;

    private final String filename;
    private final long fileLength;

    public ShareTask(String username, String token, String filename, long fileLength) {
        this.username = username;
        this.token = token;
        this.filename = filename;
        this.fileLength = fileLength;
    }

    @Override
    protected ShareResponse call() throws Exception {
        RG regenerateData = CryptoUtility.regenerateKey((int) this.fileLength, Globals.getKey());
        int[][] rg = regenerateData.getRg();
        int[][] newKey = regenerateData.getKey();

        StringBuilder rgHexs = new StringBuilder();
        for (int[] ints : rg) {
            byte[] temp = new byte[ints.length];
            System.out.print(ints.length + " ");
            for (int j = 0; j < ints.length; j++) {
                temp[j] = (byte) (ints[j] & 0xFF);
            }
            rgHexs.append(ConversionUtility.bytesToHex(temp)).append(" ");
        }
        System.out.println();


        String SERVER_URL = "http://localhost:8080/reencrypt/cloud";
        HttpPost post = new HttpPost(SERVER_URL);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("username", this.username)
                .addTextBody("token", this.token)
                .addTextBody("filename", this.filename)
                .addTextBody("rg", rgHexs.toString())
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

            if (responseEntity == null) {
                throw new RuntimeException("Internal server error. Please try again later or contact support");
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<>() {
            });
            byte[] reencryptedFile = ConversionUtility.byteArrayFromBase64((String) responseMap.get("fileBytes"));
            byte[] reencryptedIv = ConversionUtility.byteArrayFromBase64((String) responseMap.get("ivBytes"));
            String shareId = (String) responseMap.get("shareId");

            return new ShareResponse(shareId, reencryptedFile, reencryptedIv, newKey);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
