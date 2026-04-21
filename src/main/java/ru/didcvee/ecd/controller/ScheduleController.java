package ru.didcvee.ecd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@Transactional
public class ScheduleController {

    private final JdbcTemplate jdbc;

    private static final String SELECT_BASE = """
            SELECT s.id, s.group_id, s.subject_id, s.teacher_id, s.date, s.lesson_number,
                   sub.name AS subject_name,
                   u.username AS teacher_username,
                   u.firstname AS teacher_firstname,
                   u.lastname AS teacher_lastname
            FROM schedule s
            LEFT JOIN subjects sub ON sub.id = s.subject_id
            LEFT JOIN users u ON u.id = s.teacher_id
            WHERE s.group_id = ?
            """;

    /**
     * Список занятий: либо один день ({@code date}), либо диапазон ({@code date_from}, {@code date_to}) для недели.
     */
    @GetMapping
    public List<Map<String, Object>> get(
            @RequestParam("group_id") Long groupId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String date_from,
            @RequestParam(required = false) String date_to
    ) {
        if (date_from != null && !date_from.isBlank() && date_to != null && !date_to.isBlank()) {
            return jdbc.queryForList(
                    SELECT_BASE + " AND s.date BETWEEN ?::date AND ?::date ORDER BY s.date, s.lesson_number",
                    groupId,
                    date_from,
                    date_to
            );
        }
        if (date != null && !date.isBlank()) {
            return jdbc.queryForList(
                    SELECT_BASE + " AND s.date = ?::date ORDER BY s.lesson_number",
                    groupId,
                    date
            );
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Укажите параметр date или пару date_from и date_to"
        );
    }

    @PostMapping
    public void create(@RequestBody Map<String, Object> body) {
        jdbc.update("""
                        INSERT INTO schedule(group_id, subject_id, teacher_id, date, lesson_number)
                        VALUES (?, ?, ?, ?::date, ?)
                """,
                body.get("group_id"),
                body.get("subject_id"),
                body.get("teacher_id"),
                body.get("date"),
                body.get("lesson_number")
        );
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        jdbc.update("""
                        UPDATE schedule
                        SET group_id = ?, subject_id = ?, teacher_id = ?, date = ?::date, lesson_number = ?
                        WHERE id = ?
                """,
                body.get("group_id"),
                body.get("subject_id"),
                body.get("teacher_id"),
                body.get("date"),
                body.get("lesson_number"),
                id
        );
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        jdbc.update("DELETE FROM schedule WHERE id = ?", id);
    }
}
