package ru.didcvee.ecd.security.jwt.common;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface TokenGenerator {

    String generate(String username, Collection<? extends GrantedAuthority> authorities, Long id);
}
