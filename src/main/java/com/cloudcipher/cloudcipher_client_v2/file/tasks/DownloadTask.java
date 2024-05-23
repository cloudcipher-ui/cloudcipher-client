package com.cloudcipher.cloudcipher_client_v2.file.tasks;

import com.cloudcipher.cloudcipher_client_v2.file.model.DownloadResponse;
import com.cloudcipher.cloudcipher_client_v2.utility.WebUtility;
import javafx.concurrent.Task;


public class DownloadTask extends Task<DownloadResponse> {

    private final String username;
    private final String token;
    private final String filename;

    public DownloadTask(String username, String token, String filename) {
        this.username = username;
        this.token = token;
        this.filename = filename;
    }

    @Override
    protected DownloadResponse call() {
        DownloadResponse response = new DownloadResponse();

        byte[] fileBytes = download(this.filename);
        byte[] ivBytes = download("iv/" + this.filename);

        response.setFileBytes(fileBytes);
        response.setIvBytes(ivBytes);
        return response;
    }

    private byte[] download(String filename) {
        return WebUtility.downloadRequest(this.username, this.token, filename);
    }
}
