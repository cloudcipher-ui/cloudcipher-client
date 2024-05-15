package com.cloudcipher.cloudcipher_client_v2.utility;

import java.util.ArrayList;
import java.util.List;

public class ConversionUtility {

    public static byte[] longArrayToByteArray(long[][] arr) {
        List<Byte> byteList = new ArrayList<>();
        for (long[] longs : arr) {
            for (int j = 0; j < 2; j++) {
                byte[] temp = {
                    (byte) ((longs[j] >> 56) & 0xFF),
                    (byte) ((longs[j] >> 48) & 0xFF),
                    (byte) ((longs[j] >> 40) & 0xFF),
                    (byte) ((longs[j] >> 32) & 0xFF),
                    (byte) ((longs[j] >> 24) & 0xFF),
                    (byte) ((longs[j] >> 16) & 0xFF),
                    (byte) ((longs[j] >> 8) & 0xFF),
                    (byte) (longs[j] & 0xFF)
                };
                for (byte b : temp) {
                    byteList.add(b);
                }
            }
        }
        byte[] fileBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            fileBytes[i] = byteList.get(i);
        }
        return fileBytes;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
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
