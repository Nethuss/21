package ru.didcvee.ecd.security.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class SignupRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 200)
    private String firstname;

    @NotBlank
    @Size(max = 200)
    private String middlename;

    @NotBlank
    @Size(max = 200)
    private String lastname;

    private Set<String> role;

    @NotBlank
    @Size(min = 5, max = 40)
    private String password;
}
