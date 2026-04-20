package ru.didcvee.ecd.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String firstname;
    private String middlename;
    private String lastname;
    private List<String> roles;
}
