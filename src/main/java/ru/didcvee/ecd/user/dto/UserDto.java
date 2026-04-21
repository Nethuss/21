package ru.didcvee.ecd.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String firstname;
    private String middlename;
    private String lastname;
    private Boolean isActive;
    private List<String> roles;
    private List<Long> subjectIds;
    private List<SubjectDto> subjects;
    private Long groupId;
}
