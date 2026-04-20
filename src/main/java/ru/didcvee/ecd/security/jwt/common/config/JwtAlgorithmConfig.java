package ru.didcvee.ecd.security.jwt.common.config;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.didcvee.ecd.security.properties.JwtProperties;

@Configuration
public class JwtAlgorithmConfig {

    @Bean(name = "accessAlgorithm")
    public Algorithm accessAlgorithm(JwtProperties props) {
        return Algorithm.HMAC512(props.getAccessTokenSecret());
    }

    @Bean(name = "refreshAlgorithm")
    public Algorithm refreshAlgorithm(JwtProperties props) {
        return Algorithm.HMAC512(props.getRefreshTokenSecret());
    }
}
