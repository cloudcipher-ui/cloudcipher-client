package com.cloudcipher.cloudcipher_client_v2.tool.tasks;

import com.cloudcipher.cloudcipher_client_v2.Globals;
import com.cloudcipher.cloudcipher_client_v2.utility.ConversionUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.FileUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.WebUtility;
import javafx.concurrent.Task;

import java.io.File;
import java.util.Map;

public class ReceiveTask extends Task<String> {

    private final String shareId;
    private final String keyPath;

    public ReceiveTask(String shareId, String keyPath) {
        this.shareId = shareId;
        this.keyPath = keyPath;
    }

    @Override
    protected String call() throws Exception {
        Map<String, Object> responseMap = WebUtility.receiveRequest(this.shareId);

        String filename = (String) responseMap.get("filename");
        byte[] fileBytes = ConversionUtility.byteArrayFromBase64((String) responseMap.get("fileBytes"));
        byte[] ivBytes = ConversionUtility.byteArrayFromBase64((String) responseMap.get("ivBytes"));

        File keyFile = new File(this.keyPath);
        int[][] key = CryptoUtility.readSymmetricKey(keyFile);

        byte[] decryptedFileBytes = CryptoUtility.decrypt(fileBytes, key, ivBytes);

        String directory = Globals.getDefaultDirectory() + "/downloaded/";
        File dir = new File(directory);
        directory = dir.getAbsolutePath();
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }

        FileUtility.writeFile(decryptedFileBytes, directory + "/" + filename);

        return directory;
    }
}
