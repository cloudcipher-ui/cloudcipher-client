package com.cloudcipher.cloudcipher_client_v2.tool.tasks;

import com.cloudcipher.cloudcipher_client_v2.Globals;
import com.cloudcipher.cloudcipher_client_v2.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client_v2.utility.FileUtility;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;

public class DecryptTask extends Task<String> {

    private final String filePath;
    private final String ivPath;
    private final String keyPath;

    public DecryptTask(String filePath, String ivPath, String keyPath) {
        this.filePath = filePath;
        this.ivPath = ivPath;
        this.keyPath = keyPath;
    }

    @Override
    protected String call() throws IOException {
        File file = new File(this.filePath);
        File keyFile = new File(this.keyPath);
        File ivFile = new File(this.ivPath);

        byte[] fileBytes = FileUtility.readFile(file);
        byte[] iv = FileUtility.readFile(ivFile);
        int[][] key = CryptoUtility.readSymmetricKey(keyFile);

        byte[] decryptedFileBytes = CryptoUtility.decrypt(fileBytes, key, iv);

        String directory = Globals.getDefaultDirectory() + "/decrypted/";
        File dir = new File(directory);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }

        String resultFileName = file.getName().replace("_encrypted_", "_decrypted_");
        FileUtility.writeFile(decryptedFileBytes, directory + resultFileName);

        return directory;
    }
}
