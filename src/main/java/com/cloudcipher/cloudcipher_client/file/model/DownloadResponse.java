package com.cloudcipher.cloudcipher_client.file.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadResponse {

    private byte[] fileBytes;
    private byte[] ivBytes;
    private int[][] key;
}
