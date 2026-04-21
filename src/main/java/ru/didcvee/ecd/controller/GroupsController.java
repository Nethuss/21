package ru.didcvee.ecd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Transactional
public class GroupsController {

    private final JdbcTemplate jdbc;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return jdbc.queryForList("SELECT * FROM groups");
    }

    @PostMapping
    public void create(@RequestBody Map<String, Object> body) {
        jdbc.update("INSERT INTO groups(name) VALUES (?)", body.get("name"));
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        jdbc.update("UPDATE groups SET name = ? WHERE id = ?", body.get("name"), id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        jdbc.update("DELETE FROM groups WHERE id = ?", id);
    }
}
