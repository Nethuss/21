package ru.didcvee.ecd.security.repository.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class User {
    private Long id;
    private String username;
    private String password;
    private String firstname;
    private String middlename;
    private String lastname;
    private Boolean isActive;
    private List<Role> roles;
}
