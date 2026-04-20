package ru.didcvee.ecd.security.jwt.common;

import com.auth0.jwt.interfaces.DecodedJWT;

@FunctionalInterface
public interface TokenDecoder {

    DecodedJWT decode(String token);
}
