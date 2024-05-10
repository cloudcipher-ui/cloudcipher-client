package com.cloudcipher.cloudcipher_client_v2.utility;

import com.cloudcipher.cloudcipher_client_v2.utility.CloudCipher.CloudCipherUtility;
import com.cloudcipher.cloudcipher_client_v2.file.model.EncryptionResult;

import java.util.Arrays;

public class CryptoUtility {
    private static final int BLOCK_SIZE = 16;
    private static final int CTR = 0;

    public static byte[] decrypt(byte[] fileBytes, int[][] key, byte[] iv) {
        int fileLength = fileBytes.length;
        int numBlocks = fileLength / BLOCK_SIZE;
        long[][] longs = new long[numBlocks][2];

        // Convert all blocks to longs  (8 bytes chunks)
        for (int i = 0; i < numBlocks; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 8; k++) {
                    longs[i][j] <<= 8;
                    longs[i][j] |= (fileBytes[i * BLOCK_SIZE + j * 8 + k] & 0xFF);
                }
            }
        }

        try {
            CloudCipherUtility cc = new CloudCipherUtility();
            for (int i = 0; i < BLOCK_SIZE; i++) {
                cc.iv[i] = iv[i];
            }

            long[][] decrypted = cc.decrypt(key[0], key[1], key[2], CTR, longs);

            byte[] decryptedBytes = new byte[decrypted.length * BLOCK_SIZE];
            int ptr = 0;

            for (long[] value : decrypted) {
                for (int j = 0; j < 2; j++) {
                    byte[] temp = {
                            (byte) ((value[j] >> 56) & 0xFF),
                            (byte) ((value[j] >> 48) & 0xFF),
                            (byte) ((value[j] >> 40) & 0xFF),
                            (byte) ((value[j] >> 32) & 0xFF),
                            (byte) ((value[j] >> 24) & 0xFF),
                            (byte) ((value[j] >> 16) & 0xFF),
                            (byte) ((value[j] >> 8) & 0xFF),
                            (byte) (value[j] & 0xFF)};
                    System.arraycopy(temp, 0, decryptedBytes, ptr, temp.length);
                    ptr += 8;
                }
            }

            int padding = decryptedBytes[decryptedBytes.length - 1];
            boolean stolen = true;
            for (int i = 0; i < padding; i++) {
                if (decryptedBytes[decryptedBytes.length - 1 - i] != padding) {
                    stolen = false;
                    break;
                }
            }
            if (stolen) {
                byte[] temp = new byte[decryptedBytes.length];
                System.arraycopy(decryptedBytes, 0, temp, 0, decryptedBytes.length);
                decryptedBytes = new byte[decryptedBytes.length - padding];
                System.out.println(temp.length + " " + decryptedBytes.length + " " + padding);
                System.arraycopy(temp, 0, decryptedBytes, 0, decryptedBytes.length);
            }

            return decryptedBytes;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static EncryptionResult encrypt(byte[] fileBytes, int[][] key) {
        int fileLength = fileBytes.length;
        int numBlocks = fileLength / BLOCK_SIZE;
        int remainder = fileLength % BLOCK_SIZE;

        long[][] longs = new long[numBlocks + 1][2];

        if (remainder != 0) {
            byte[] padding = new byte[BLOCK_SIZE - remainder];
            Arrays.fill(padding, (byte) (BLOCK_SIZE - remainder));

            byte[] lastBlock = new byte[BLOCK_SIZE];
            System.arraycopy(fileBytes, numBlocks * BLOCK_SIZE, lastBlock, 0, remainder);
            System.arraycopy(padding, 0, lastBlock, remainder, padding.length);


            // Convert last block to longs  (8 bytes chunks)
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 8; k++) {
                    longs[numBlocks][j] <<= 8;
                    longs[numBlocks][j] |= (lastBlock[j * 8 + k] & 0xFF);
                }
            }
        }

        // Convert all blocks to longs  (8 bytes chunks)
        for (int i = 0; i < numBlocks; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 8; k++) {
                    longs[i][j] <<= 8;
                    longs[i][j] |= (fileBytes[i * BLOCK_SIZE + j * 8 + k] & 0xFF);
                }
            }
        }

        try {
            CloudCipherUtility cc = new CloudCipherUtility();
            long[][] encrypted = cc.encrypt(key[0], key[1], key[2], CTR, longs);

            byte[] iv = new byte[BLOCK_SIZE];
            for (int i = 0; i < BLOCK_SIZE; i++) {
                iv[i] = (byte) (cc.iv[i] & 0xFF);
            }

            return new EncryptionResult(encrypted, iv);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
