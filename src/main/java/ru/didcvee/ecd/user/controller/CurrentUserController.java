package ru.didcvee.ecd.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.didcvee.ecd.model.CurrentUser;
import ru.didcvee.ecd.service.CurrentUserProvider;
import ru.didcvee.ecd.user.dto.UserDto;
import ru.didcvee.ecd.user.service.AdminUserService;

@RestController
@RequiredArgsConstructor
@RequestMapping(("/user"))
public class CurrentUserController {

    private final CurrentUserProvider currentUserProvider;
    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<UserDto> currentUser(){
        CurrentUser currentUser = currentUserProvider.get();
        return ResponseEntity.ok(adminUserService.getOne(currentUser.id()));
    }
}
