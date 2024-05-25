package com.cloudcipher.cloudcipher_client.tool.tasks;

import com.cloudcipher.cloudcipher_client.file.model.EncryptionResult;
import com.cloudcipher.cloudcipher_client.file.model.ShareResponse;
import com.cloudcipher.cloudcipher_client.utility.*;
import javafx.concurrent.Task;

import java.io.File;

public class ShareLocalTask extends Task<ShareResponse> {

    private final String filePath;
    private final String keyPath;

    public ShareLocalTask(String filePath, String keyPath) {
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

        return WebUtility.shareLocalRequest(file.getName(), encryptedFileBytes, iv, rgHexs.toString(), newKey);
    }
}
