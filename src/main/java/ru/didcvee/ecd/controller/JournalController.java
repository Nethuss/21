package ru.didcvee.ecd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.didcvee.ecd.service.CurrentUserProvider;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JournalController {

    private final JdbcTemplate jdbc;
    private final CurrentUserProvider auth;

    @GetMapping("/journal")
    public Map<String, Object> getJournal(
            @RequestParam Long group_id,
            @RequestParam Long subject_id
    ) {
        Long teacherId = auth.get().id();

        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM teacher_subjects
            WHERE subject_id = ? AND teacher_id = ?
        """, Integer.class, subject_id, teacherId);

        if (count == 0) throw new RuntimeException("Access denied");

        var students = jdbc.queryForList("""
            SELECT * FROM users WHERE group_id = ?
        """, group_id);

        var lessons = jdbc.queryForList("""
            SELECT * FROM schedule
            WHERE group_id = ? AND subject_id = ?
            ORDER BY date
        """, group_id, subject_id);

        var grades = jdbc.queryForList("""
            SELECT * FROM grades
            WHERE schedule_id IN (
                SELECT id FROM schedule
                WHERE group_id = ? AND subject_id = ?
            )
        """, group_id, subject_id);

        return Map.of(
                "students", students,
                "lessons", lessons,
                "grades", grades
        );
    }
}