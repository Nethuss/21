package ru.didcvee.ecd.security.jwt.access;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.didcvee.ecd.security.jwt.common.TokenValidator;

@Component
public class AccessTokenValidator implements TokenValidator {

    private final JWTVerifier verifier;

    public AccessTokenValidator(@Qualifier("accessVerifier") JWTVerifier accessVerifier) {
        this.verifier = accessVerifier;
    }

    @Override
    public boolean validate(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
