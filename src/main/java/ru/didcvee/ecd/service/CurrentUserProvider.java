package ru.didcvee.ecd.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.didcvee.ecd.model.CurrentUser;
import ru.didcvee.ecd.security.service.UserDetailsImpl;

@Component
public class CurrentUserProvider {

    public CurrentUser get() {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return new CurrentUser(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getLastName(),
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
        );
    }
}