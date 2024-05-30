package com.cloudcipher.cloudcipher_client.file.tasks;

import com.cloudcipher.cloudcipher_client.Globals;
import com.cloudcipher.cloudcipher_client.file.model.ShareResponse;
import com.cloudcipher.cloudcipher_client.utility.ConversionUtility;
import com.cloudcipher.cloudcipher_client.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client.utility.RG;
import com.cloudcipher.cloudcipher_client.utility.WebUtility;
import javafx.concurrent.Task;

public class ShareCloudTask extends Task<ShareResponse> {

    private final String username;
    private final String token;

    private final String filename;
    private final long fileLength;

    public ShareCloudTask(String username, String token, String filename, long fileLength) {
        this.username = username;
        this.token = token;
        this.filename = filename;
        this.fileLength = fileLength;
    }

    @Override
    protected ShareResponse call() throws Exception {
        long startTime = System.nanoTime();
        RG regenerateData = CryptoUtility.regenerateKey((int) this.fileLength, Globals.getKey());
        long endTime = System.nanoTime();
        System.out.println("Regeneration Time: " + (endTime - startTime) + " ns");

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

        return WebUtility.shareCloudRequest(username, token, filename, rgHexs.toString(), newKey);
    }
}
