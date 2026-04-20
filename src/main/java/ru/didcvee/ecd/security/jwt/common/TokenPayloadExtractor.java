package ru.didcvee.ecd.security.jwt.common;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public interface TokenPayloadExtractor {

    String getUsername(DecodedJWT jwt);

    List<SimpleGrantedAuthority> getRoles(DecodedJWT jwt);

    Long getId(DecodedJWT jwt);
}
