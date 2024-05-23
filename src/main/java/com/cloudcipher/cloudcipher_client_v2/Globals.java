package com.cloudcipher.cloudcipher_client_v2;

import javafx.stage.Window;
import lombok.Getter;
import lombok.Setter;

import java.security.KeyPair;

public class Globals {

    @Getter
    @Setter
    private static String token;

    @Getter
    @Setter
    private static String username;

    @Getter
    @Setter
    private static String defaultDirectory;

    @Getter
    @Setter
    private static int[][] key;

    @Getter
    @Setter
    private static KeyPair keyPair;

    @Getter
    @Setter
    private static Window primaryStage;

    @Getter
    private static long maxSize = 1024 * 1024 * 1024L;

    @Getter
    private static String serverUrl = "https://3.215.250.196:8080/";
//    private static String serverUrl = "http://localhost:8080/";
}
