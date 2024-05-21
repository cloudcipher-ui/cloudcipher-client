package com.cloudcipher.cloudcipher_client_v2.tool.tasks;

import com.cloudcipher.cloudcipher_client_v2.file.model.EncryptionResult;
import com.cloudcipher.cloudcipher_client_v2.file.model.ShareResponse;
import com.cloudcipher.cloudcipher_client_v2.utility.ConversionUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.FileUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.RG;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.util.Map;

public class SendTask extends Task<ShareResponse> {

    private final String filePath;
    private final String keyPath;

    public SendTask(String filePath, String keyPath) {
        this.filePath = filePath;
        this.keyPath = keyPath;
    }

    @Override
    protected ShareResponse call() throws Exception {
        File file = new File(this.filePath);
        File keyFile = new File(this.keyPath);

        byte[] fileBytes = FileUtility.readFile(file);
        int[][] key = CryptoUtility.readSymmetricKey(keyFile);

        EncryptionResult result = CryptoUtility.encrypt(fileBytes, file.length(), key);
        long[][] encryptedFile = result.getEncryptedFile();
        byte[] encryptedFileBytes = ConversionUtility.longArrayToByteArray(encryptedFile);
        byte[] iv = result.getIv();

        RG regenerateData = CryptoUtility.regenerateKey(encryptedFileBytes.length, key);
        int[][] rg = regenerateData.getRg();
        int[][] newKey = regenerateData.getKey();

        StringBuilder rgHexs = new StringBuilder();
        for (int[] ints : rg) {
            byte[] temp = new byte[ints.length];
            for (int j = 0; j < ints.length; j++) {
                temp[j] = (byte) (ints[j] & 0xFF);
            }
            rgHexs.append(ConversionUtility.bytesToHex(temp)).append(" ");
        }

        String SERVER_URL = "http://localhost:8080/reencrypt/local";
        HttpPost post = new HttpPost(SERVER_URL);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", encryptedFileBytes, ContentType.APPLICATION_OCTET_STREAM, file.getName())
                .addBinaryBody("iv", iv, ContentType.APPLICATION_OCTET_STREAM, "iv")
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
