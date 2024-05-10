package com.cloudcipher.cloudcipher_client_v2.file.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadResponse {

    private byte[] fileBytes;
    private byte[] ivBytes;
    private int[][] key;
}
