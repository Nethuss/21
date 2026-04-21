package ru.didcvee.ecd.security.jwt.access;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;
import ru.didcvee.ecd.security.jwt.common.*;
import ru.didcvee.ecd.security.service.UserDetailsImpl;

@Service
@RequiredArgsConstructor
public class AccessTokenFacade implements TokenFacade {

    private final TokenGenerator generator;
    private final TokenDecoder decoder;
    private final TokenPayloadExtractor extractor;
    private final TokenValidator validator;

    @Value("${jwt.cookieName}")
    private String jwtCookie;

    @Value("${jwt.jwtRefreshCookieName}")
    private String jwtRefreshCookie;

    @Override
    public String createToken(UserDetailsImpl userDetails) {
        return generator.generate(userDetails.getUsername(), userDetails.getAuthorities(), userDetails.getId());
    }

    @Override
    public boolean validate(String token) {
        return validator.validate(token);
    }

    @Override
    public UserDetailsImpl parse(String token) {
        DecodedJWT jwt = decoder.decode(token);
        return UserDetailsImpl.builder()
                .username(extractor.getUsername(jwt))
                .authorities(extractor.getRoles(jwt))
                .id(extractor.getId(jwt))
                .build();
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = createToken(userPrincipal);
        return generateCookie(jwtCookie, jwt, "/ecd");
    }

    public ResponseCookie generateRefreshJwtCookie(String refreshToken) {
        return generateCookie(jwtRefreshCookie, refreshToken, "/ecd/auth/refreshtoken");
    }

    public String getJwtFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request, jwtCookie);
    }

    public String getJwtRefreshFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request, jwtRefreshCookie);
    }

    public ResponseCookie getCleanJwtCookie() {
        ResponseCookie cookie =
                ResponseCookie.from(jwtCookie, null).path("/ecd").build();
        return cookie;
    }

    public ResponseCookie getCleanJwtRefreshCookie() {
        ResponseCookie cookie = ResponseCookie.from(jwtRefreshCookie, null)
                .path("/ecd/auth/refreshtoken")
                .build();
        return cookie;
    }

    private ResponseCookie generateCookie(String name, String value, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path(path)
                .maxAge(24 * 60 * 60)
                .httpOnly(true)
                .build();
        return cookie;
    }

    private String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }
}
