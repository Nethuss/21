package ru.didcvee.ecd.security.jwt.access;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import ru.didcvee.ecd.security.jwt.common.TokenGenerator;
import ru.didcvee.ecd.security.properties.JwtProperties;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccessTokenGenerator implements TokenGenerator {

    private final Algorithm algorithm;
    private final long expirationMs;
    private final String issuer;

    public AccessTokenGenerator(
            @Qualifier("accessAlgorithm") Algorithm accessAlgorithm,
            JwtProperties properties,
            @Value("${jwt.issuer}") String issuer) {
        this.algorithm = accessAlgorithm;
        this.expirationMs = properties.getAccessTokenExpirationMinutes() * 60_000L;
        this.issuer = issuer;
    }

    @Override
    public String generate(String username, Collection<? extends GrantedAuthority> authorities, Long id) {
        // Извлекаем роли из authorities и преобразуем в список строк
        List<String> roles =
                authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
                .withClaim("roles", roles) // Добавляем роли в токен
                .withClaim("id", id) // Добавляем ID пользователя
                .sign(algorithm);
    }
}
