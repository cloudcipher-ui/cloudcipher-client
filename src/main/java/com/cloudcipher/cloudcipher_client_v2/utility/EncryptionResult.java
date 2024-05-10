package com.cloudcipher.cloudcipher_client_v2.utility;

import lombok.Getter;

@Getter
public class EncryptionResult {
    public final long[][] encryptedFile;
    public final byte[] iv;

    public EncryptionResult(long[][] encryptedFile, byte[] iv) {
        this.encryptedFile = encryptedFile;
        this.iv = iv;
    }
}
