package ru.didcvee.ecd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.didcvee.ecd.service.CurrentUserProvider;

import java.util.Map;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
public class GradesController {

    private final JdbcTemplate jdbc;
    private final CurrentUserProvider auth;

    @PutMapping
    public void upsert(@RequestBody Map<String, Object> body) {

        Long teacherId = auth.get().id();

        // access check
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM schedule s
            WHERE s.id = ? AND s.teacher_id = ?
        """, Integer.class,
                body.get("schedule_id"),
                teacherId
        );

        if (count == 0) throw new RuntimeException("Access denied");

        jdbc.update("""
            INSERT INTO grades(student_id, schedule_id, value, comment)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (student_id, schedule_id)
            DO UPDATE SET value = EXCLUDED.value,
                          comment = EXCLUDED.comment
        """,
                body.get("student_id"),
                body.get("schedule_id"),
                body.get("value"),
                body.get("comment")
        );
    }
}
