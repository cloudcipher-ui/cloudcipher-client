package com.cloudcipher.cloudcipher_client_v2.utility;

import com.cloudcipher.cloudcipher_client_v2.Globals;

import java.io.*;

public class FileUtility {

    public static String getApplicationPath() {
        return System.getProperty("user.home") + "/cloudcipher/";
    }

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

    public static void saveConfig() throws IOException {
        File configFile = new File(getApplicationPath() + "/config");
        try (FileWriter writer = new FileWriter(configFile)) {
            if (Globals.getDefaultDirectory() != null) {
                writer.write("defaultDirectory=" + Globals.getDefaultDirectory() + "\n");
            }
            if (Globals.getUsername() != null) {
                writer.write("username=" + Globals.getUsername() + "\n");
            }
            if (Globals.getToken() != null) {
                writer.write("token=" + Globals.getToken() + "\n");
            }
        }
    }

    public static void loadConfig() throws IOException {
        File configFile = new File(getApplicationPath() + "/config");
        if (!configFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length != 2) {
                    throw new IOException("Invalid config file");
                }

                switch (parts[0]) {
                    case "defaultDirectory":
                        Globals.setDefaultDirectory(parts[1]);
                        break;
                    case "username":
                        Globals.setUsername(parts[1]);
                        break;
                    case "token":
                        Globals.setToken(parts[1]);
                        break;
                    default:
                }
            }
        }
    }

    public static void loadSymmetricKey() throws IOException {
        File keyFile = new File(getApplicationPath() + "/" + Globals.getUsername() + ".key");
        if (!keyFile.exists()) {
            return;
        }

        int[][] key = CryptoUtility.readSymmetricKey(keyFile);
        Globals.setKey(key);
    }
}
