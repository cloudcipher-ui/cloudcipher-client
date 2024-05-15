package com.cloudcipher.cloudcipher_client_v2.utility;

public class ConversionUtility {
    private static final int BLOCK_SIZE = 16;

    public static byte[] longArrayToByteArray(long[][] arr) {
        int numBlocks = arr.length;
        byte[] fileBytes = new byte[numBlocks * BLOCK_SIZE];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < 2; j++) {
                byte[] temp = {
                        (byte) ((arr[i][j] >> 56) & 0xFF),
                        (byte) ((arr[i][j] >> 48) & 0xFF),
                        (byte) ((arr[i][j] >> 40) & 0xFF),
                        (byte) ((arr[i][j] >> 32) & 0xFF),
                        (byte) ((arr[i][j] >> 24) & 0xFF),
                        (byte) ((arr[i][j] >> 16) & 0xFF),
                        (byte) ((arr[i][j] >> 8) & 0xFF),
                        (byte) (arr[i][j] & 0xFF)
                };
                System.arraycopy(temp, 0, fileBytes, i * BLOCK_SIZE + j * 8, 8);
            }
        }
        return fileBytes;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b: bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hexString) {
        int arrLength = hexString.length() / 2;
        byte[] byteArray = new byte[arrLength];

        for (int i = 0; i < arrLength; i++) {
            int idx = i * 2;
            int v = Integer.parseInt(hexString.substring(idx, idx + 2), 16);
            byteArray[i] = (byte) v;
        }
        return byteArray;
    }

    public static byte[] byteArrayFromBase64(String fileBytes) {
        return java.util.Base64.getDecoder().decode(fileBytes);
    }
}
