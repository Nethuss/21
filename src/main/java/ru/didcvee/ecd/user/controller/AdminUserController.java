package ru.didcvee.ecd.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.didcvee.ecd.user.dto.CreateUserRequest;
import ru.didcvee.ecd.user.dto.UpdateUserRequest;
import ru.didcvee.ecd.user.dto.UserDto;
import ru.didcvee.ecd.user.service.AdminUserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService service;

    @GetMapping
    public List<UserDto> all() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public UserDto one(@PathVariable Long id) {
        return service.getOne(id);
    }

    @PostMapping
    public void create(@RequestBody CreateUserRequest req) {
        service.create(req);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id,
                       @RequestBody UpdateUserRequest req) {
        service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
