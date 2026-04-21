package ru.didcvee.ecd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.didcvee.ecd.service.CurrentUserProvider;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final JdbcTemplate jdbc;
    private final CurrentUserProvider auth;

    @GetMapping("/schedule")
    public List<Map<String, Object>> schedule() {
        Long userId = auth.get().id();

        Integer groupId = jdbc.queryForObject("""
            SELECT group_id FROM users WHERE id = ?
        """, Integer.class, userId);

        return jdbc.queryForList("""
            SELECT * FROM schedule
            WHERE group_id = ?
            ORDER BY date, lesson_number
        """, groupId);
    }

    @GetMapping("/grades")
    public List<Map<String, Object>> grades() {
        Long userId = auth.get().id();

        return jdbc.queryForList("""
            SELECT g.*, s.date, s.lesson_number
            FROM grades g
            JOIN schedule s ON s.id = g.schedule_id
            WHERE g.student_id = ?
        """, userId);
    }
}
