package ru.didcvee.ecd.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String accessTokenSecret;
    private String refreshTokenSecret;
    private Integer refreshTokenExpirationDays;
    private Integer accessTokenExpirationMinutes;
}
