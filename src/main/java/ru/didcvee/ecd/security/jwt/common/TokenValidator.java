package ru.didcvee.ecd.security.jwt.common;

@FunctionalInterface
public interface TokenValidator {

    boolean validate(String token);
}
