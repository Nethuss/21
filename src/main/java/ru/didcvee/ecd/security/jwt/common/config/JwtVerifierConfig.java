package ru.didcvee.ecd.security.jwt.common.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtVerifierConfig {

    @Bean(name = "accessVerifier")
    public JWTVerifier accessTokenVerifier(
            @Qualifier("accessAlgorithm") Algorithm accessAlgorithm, @Value("${jwt.issuer}") String issuer) {
        return JWT.require(accessAlgorithm).withIssuer(issuer).build();
    }

    @Bean(name = "refreshVerifier")
    public JWTVerifier refreshTokenVerifier(
            @Qualifier("refreshAlgorithm") Algorithm accessAlgorithm, @Value("${jwt.issuer}") String issuer) {
        return JWT.require(accessAlgorithm).withIssuer(issuer).build();
    }
}
