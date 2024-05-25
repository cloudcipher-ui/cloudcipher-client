package com.cloudcipher.cloudcipher_client.file.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareResponse {

    private String shareId;
    private byte[] fileBytes;
    private byte[] ivBytes;
    private int[][] newKey;

    public ShareResponse(String shareId, byte[] reencryptedFile, byte[] reencryptedIv, int[][] newKey) {
        this.shareId = shareId;
        this.fileBytes = reencryptedFile;
        this.ivBytes = reencryptedIv;
        this.newKey = newKey;
    }
}
