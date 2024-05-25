package com.cloudcipher.cloudcipher_client.file.tasks;

import com.cloudcipher.cloudcipher_client.Globals;
import com.cloudcipher.cloudcipher_client.utility.ConversionUtility;
import com.cloudcipher.cloudcipher_client.utility.CryptoUtility;
import com.cloudcipher.cloudcipher_client.file.model.EncryptionResult;
import com.cloudcipher.cloudcipher_client.utility.FileUtility;
import com.cloudcipher.cloudcipher_client.utility.WebUtility;
import javafx.concurrent.Task;

import java.io.File;

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
        EncryptionResult result = CryptoUtility.encrypt(fileBytes, this.file.length(), Globals.getKey());

        long[][] encryptedFile = result.getEncryptedFile();
        byte[] encryptedFileBytes = ConversionUtility.longArrayToByteArray(encryptedFile);
        byte[] iv = result.getIv();

        return WebUtility.uploadRequest(username, token, encryptedFileBytes, iv, this.file.getName());
    }
}
