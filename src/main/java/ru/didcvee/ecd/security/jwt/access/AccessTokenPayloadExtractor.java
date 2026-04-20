package ru.didcvee.ecd.security.jwt.access;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import ru.didcvee.ecd.security.jwt.common.TokenPayloadExtractor;

import java.util.List;

@Component
public class AccessTokenPayloadExtractor implements TokenPayloadExtractor {

    @Override
    public String getUsername(DecodedJWT jwt) {
        return jwt.getSubject();
    }

    @Override
    public List<SimpleGrantedAuthority> getRoles(DecodedJWT jwt) {
        return jwt.getClaim("roles").asList(SimpleGrantedAuthority.class);
    }

    @Override
    public Long getId(DecodedJWT jwt) {
        return jwt.getClaim("id").asLong();
    }
}
