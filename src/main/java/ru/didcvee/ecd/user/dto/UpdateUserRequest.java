package ru.didcvee.ecd.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
    private String password; // nullable
    private String firstname;
    private String middlename;
    private String lastname;
    private Boolean isActive;
    private List<String> roles;
}