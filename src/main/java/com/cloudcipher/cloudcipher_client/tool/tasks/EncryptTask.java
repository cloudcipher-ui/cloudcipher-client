package com.cloudcipher.cloudcipher_client.tool.tasks;

import com.cloudcipher.cloudcipher_client.Globals;
import com.cloudcipher.cloudcipher_client.file.model.EncryptionResult;
import com.cloudcipher.cloudcipher_client.utility.ConversionUtility;
import com.cloudcipher.cloudcipher_client.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;

public class EncryptTask extends Task<String> {

    private final String filePath;
    private final String keyPath;

    public EncryptTask(String filePath, String keyPath) {
        this.filePath = filePath;
        this.keyPath = keyPath;
    }

    @Override
    protected String call() throws IOException {
        File file = new File(this.filePath);
        File keyFile = new File(this.keyPath);

        byte[] fileBytes = FileUtility.readFile(file);
        int[][] key = CryptoUtility.readSymmetricKey(keyFile);

        EncryptionResult result = CryptoUtility.encrypt(fileBytes, file.length(), key);
        long[][] encryptedFile = result.getEncryptedFile();
        byte[] encryptedFileBytes = ConversionUtility.longArrayToByteArray(encryptedFile);
        byte[] iv = result.getIv();

        String directory = Globals.getDefaultDirectory() + "/encrypted/";
        File dir = new File(directory);
        directory = dir.getAbsolutePath();
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }

        String date = java.time.LocalDate.now().toString();
        String[] fileNameAndExtension = file.getName().split("\\.");
        String resultFileName = fileNameAndExtension[0] + "_encrypted_" + date + "." + fileNameAndExtension[1];
        String resultIvName = fileNameAndExtension[0] + "_iv_" + date + ".iv";

        FileUtility.writeFile(encryptedFileBytes, directory + "/" + resultFileName);
        FileUtility.writeFile(iv, directory + "/" + resultIvName);

        return directory;
    }
}
