package com.cloudcipher.cloudcipher_client_v2.utility;

import com.cloudcipher.cloudcipher_client_v2.Globals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtility {

    public static void saveDownload(byte[] fileBytes, String filename) {
        File downloadFile = new File(Globals.getDefaultDirectory() + "/" + filename);
        if (filename.contains("/")) {
            String[] parts = filename.split("/");
            String directory = Globals.getDefaultDirectory();
            for (int i = 0; i < parts.length - 1; i++) {
                directory += "/" + parts[i];
                File dir = new File(directory);
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        throw new RuntimeException("Failed to create directory: " + directory);
                    }
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(downloadFile)) {
            fos.write(fileBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String parseFileSize(double size) {
        String sizeUnit = "B";
        if (size >= 1024) {
            size /= 1024;
            sizeUnit = "KB";
            if (size >= 1024) {
                size /= 1024;
                sizeUnit = "MB";
                if (size >= 1024) {
                    size /= 1024;
                    sizeUnit = "GB";
                }
            }
        }

        size = Math.round(size * 100.0) / 100.0;
        return size + " " + sizeUnit;
    }

    public static byte[] readFile(File file) {
        int fileLength = (int) file.length();
        byte[] fileBytes = new byte[fileLength];
        try (FileInputStream fis = new FileInputStream(file)) {
            int read = fis.read(fileBytes);
            if (read != fileLength) {
                throw new IOException("File read error");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileBytes;
    }
}
