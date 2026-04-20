package ru.didcvee.ecd.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.didcvee.ecd.security.repository.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Builder
@Getter
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    private String firstName;

    private String middleName;

    private String lastName;

    private Boolean isActive;

    public UserDetailsImpl(
            Long id,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            String firstName,
            String middleName,
            String lastName,
            Boolean isActive) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.isActive = isActive;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                user.getFirstname(),
                user.getMiddlename(),
                user.getLastname(),
                user.getIsActive());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
