package ru.didcvee.ecd.security.repository.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RefreshToken {
    private Long id;
    private Long userId;
    private String token;
    private Instant expiryDate;
}
