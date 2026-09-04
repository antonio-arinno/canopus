package com.arinno.canopus.auth;

import javax.crypto.SecretKey;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class TokenJwtConfig {

    public static final String CONTENT_TYPE = "application/json";
    public static final String PREFIX_TOKEN = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static SecretKey secretKey(String encodedSecret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
    }

}
