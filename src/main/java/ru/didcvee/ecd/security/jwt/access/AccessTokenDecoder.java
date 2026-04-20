package ru.didcvee.ecd.security.jwt.access;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.didcvee.ecd.security.jwt.common.TokenDecoder;

@Component
public class AccessTokenDecoder implements TokenDecoder {

    private final JWTVerifier verifier;

    public AccessTokenDecoder(@Qualifier("accessVerifier") JWTVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public DecodedJWT decode(String token) {
        return verifier.verify(token);
    }
}
