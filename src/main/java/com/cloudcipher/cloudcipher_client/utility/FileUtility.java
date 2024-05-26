package com.cloudcipher.cloudcipher_client.utility;

import com.cloudcipher.cloudcipher_client.Globals;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtility {

    public static String getApplicationPath() {
        return System.getProperty("user.home") + "/cloudcipher/";
    }

    public static void saveDownload(byte[] fileBytes, String filename) {
        File downloadFile = new File(Globals.getDefaultDirectory() + "/downloaded/" + filename);
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
                        File dir = new File(parts[1]);
                        if (dir.exists()) {
                            Globals.setDefaultDirectory(parts[1]);
                        } else {
                            Globals.setDefaultDirectory(null);
                        }
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

    public static void writeFile(byte[] fileBytes, String path) {
        File file = new File(path);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createDirectory(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }
    }

    public static void writeKeyFile(int[][] key, String path) {
        byte[][] keyBytes = new byte[3][16];
        for (int i = 0; i < 16; i++) {
            keyBytes[0][i] = (byte) (key[0][i] & 0xFF);
            keyBytes[1][i] = (byte) (key[1][i] & 0xFF);
            keyBytes[2][i] = (byte) (key[2][i] & 0xFF);
        }

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(ConversionUtility.bytesToHex(keyBytes[0]) + "\n");
            writer.write(ConversionUtility.bytesToHex(keyBytes[1]) + "\n");
            writer.write(ConversionUtility.bytesToHex(keyBytes[2]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveDefaultDirectory(String selectedDirectory) {
        Globals.setDefaultDirectory(selectedDirectory);
        FileUtility.createDirectory(selectedDirectory + "/downloaded");
        FileUtility.createDirectory(selectedDirectory + "/encrypted");
        FileUtility.createDirectory(selectedDirectory + "/decrypted");
        FileUtility.createDirectory(selectedDirectory + "/shared");

        try {
            FileUtility.saveConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void openDirectory(String directory) {
        Path part = Paths.get(directory);
        Globals.getHostServices().showDocument(part.toUri().toString());
    }
}
