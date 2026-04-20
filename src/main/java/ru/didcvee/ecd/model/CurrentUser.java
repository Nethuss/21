package ru.didcvee.ecd.model;

import java.util.List;

public record CurrentUser(
        Long id,
        String username,
        String firstName,
        String middleName,
        String lastName,
        List<String> roles
) {}
