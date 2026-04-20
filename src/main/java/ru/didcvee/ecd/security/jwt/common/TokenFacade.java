package ru.didcvee.ecd.security.jwt.common;

import ru.didcvee.ecd.security.service.UserDetailsImpl;

public interface TokenFacade {

    String createToken(UserDetailsImpl userDetails);

    boolean validate(String token);

    UserDetailsImpl parse(String token);
}
