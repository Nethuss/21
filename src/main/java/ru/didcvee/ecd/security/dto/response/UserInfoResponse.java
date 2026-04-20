package ru.didcvee.ecd.security.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String username;
    private List<String> roles;
}
