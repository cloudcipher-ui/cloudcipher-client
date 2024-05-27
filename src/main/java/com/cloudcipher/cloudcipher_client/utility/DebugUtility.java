package com.cloudcipher.cloudcipher_client.utility;

import com.cloudcipher.cloudcipher_client.file.model.EncryptionResult;

import java.util.ArrayList;
import java.util.Arrays;

public class DebugUtility {

    private static final int BLOCK_SIZE = 16;
    private static final int[] BLOCK_COUNTS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300, 400, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000};
    private static final int[][] KEY = {
            {60, 246, 244, 75, 118, 225, 104, 147, 68, 200, 95, 82, 236, 29, 219, 54},
            {86, 109, 211, 241, 212, 180, 93, 10, 71, 150, 70, 74, 184, 33, 68, 236},
            {51, 41, 3, 55, 5, 50, 84, 197, 40, 182, 54, 64, 201, 226, 153, 176}
    };
    private static final int ITERATIONS = 100;

    public static byte[] createTestFile(int blockCount) {
        byte[] testFile = new byte[blockCount * BLOCK_SIZE];
        for (int i = 0; i < blockCount * BLOCK_SIZE; i++) {
            testFile[i] = (byte) (i % 256);
        }
        return testFile;
    }

    public static int getHash(byte[] data) {
        return Arrays.hashCode(data);
    }

    public static void encryptTest() {
        System.out.println("=== Starting encryption test ===");
        String filename = "encrypt_test.txt";
        long[] durations = new long[BLOCK_COUNTS.length];

        for (int i = 0; i < BLOCK_COUNTS.length; i++) {
            int blockCount = BLOCK_COUNTS[i];
            byte[] fileBytes = createTestFile(blockCount);
            int hash = getHash(fileBytes);

            ArrayList<Long> localDurations = new ArrayList<>();
            for (int j = 0; j < ITERATIONS; j++) {
                long startTime = System.nanoTime();
                EncryptionResult result = CryptoUtility.encrypt(fileBytes, fileBytes.length, KEY);
                long endTime = System.nanoTime();
                localDurations.add(endTime - startTime);

                long[][] encryptedFile = result.getEncryptedFile();
                byte[] encryptedFileBytes = ConversionUtility.longArrayToByteArray(encryptedFile);

                // Verify that the hash of the encrypted file is different from the hash of the original file
                int encryptedHash = getHash(encryptedFileBytes);
                if (hash == encryptedHash) {
                    throw new RuntimeException("Hashes are the same for " + blockCount + " blocks");
                }
            }

            long sum = 0;
            for (long duration : localDurations) {
                sum += duration;
            }
            durations[i] = sum / ITERATIONS;

            System.out.println("Encryption test for " + blockCount + " blocks complete");
        }

        // Saving the durations to a file
        FileUtility.saveDurations(durations, filename);
        System.out.println("=== Encryption test complete ===\n");
    }

    public static void decryptTest() {
        System.out.println("=== Starting decryption test ===");
        String filename = "decrypt_test.txt";
        long[] durations = new long[BLOCK_COUNTS.length];

        for (int i = 0; i < BLOCK_COUNTS.length; i++) {
            int blockCount = BLOCK_COUNTS[i];
            byte[] fileBytes = createTestFile(blockCount);
            int hash = getHash(fileBytes);

            EncryptionResult result = CryptoUtility.encrypt(fileBytes, fileBytes.length, KEY);
            long[][] encryptedFile = result.getEncryptedFile();
            byte[] encryptedFileBytes = ConversionUtility.longArrayToByteArray(encryptedFile);
            byte[] iv = result.getIv();

            ArrayList<Long> localDurations = new ArrayList<>();
            for (int j = 0; j < ITERATIONS; j++) {
                long startTime = System.nanoTime();
                byte[] decryptedFileBytes = CryptoUtility.decrypt(encryptedFileBytes, KEY, iv);
                long endTime = System.nanoTime();
                localDurations.add(endTime - startTime);

                // Remove trailing zeros from the decrypted file
                int length = fileBytes.length;
                for (int k = length; k > 0; k--) {
                    if (decryptedFileBytes[k - 1] != 0) {
                        length = k;
                        break;
                    }
                }
                decryptedFileBytes = Arrays.copyOf(decryptedFileBytes, length);

                // Verify that the hash of the decrypted file is the same as the hash of the original file
                int decryptedHash = getHash(decryptedFileBytes);
                if (hash != decryptedHash) {
                    System.out.println(Arrays.toString(fileBytes));
                    System.out.println(Arrays.toString(decryptedFileBytes));
                    throw new RuntimeException("Hashes are different for " + blockCount + " blocks");
                }
            }

            long sum = 0;
            for (long duration : localDurations) {
                sum += duration;
            }
            durations[i] = sum / ITERATIONS;
            System.out.println("Decryption test for " + blockCount + " blocks complete");
        }

        // Saving the durations to a file
        FileUtility.saveDurations(durations, filename);
        System.out.println("=== Decryption test complete ===\n");
    }
}
