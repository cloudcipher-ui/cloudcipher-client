package com.cloudcipher.cloudcipher_client_v2;

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
}