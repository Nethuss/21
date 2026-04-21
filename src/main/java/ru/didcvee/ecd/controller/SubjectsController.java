package ru.didcvee.ecd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
@Transactional
public class SubjectsController {

    private final JdbcTemplate jdbc;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return jdbc.queryForList("SELECT * FROM subjects");
    }

    @PostMapping
    public void create(@RequestBody Map<String, Object> body) {
        jdbc.update("""
        INSERT INTO subjects(name)
        VALUES (?)
    """,
                body.get("name")
        );
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        jdbc.update("""
        UPDATE subjects SET name = ?
        WHERE id = ?
    """,
                body.get("name"),
                id
        );
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        jdbc.update("DELETE FROM subjects WHERE id = ?", id);
    }
}
