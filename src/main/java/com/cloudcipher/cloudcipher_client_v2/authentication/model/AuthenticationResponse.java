package com.cloudcipher.cloudcipher_client_v2.authentication.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationResponse {

    private String success;
    private String token;
    private String username;
}
