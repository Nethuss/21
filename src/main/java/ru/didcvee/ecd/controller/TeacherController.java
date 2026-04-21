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
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final JdbcTemplate jdbc;
    private final CurrentUserProvider auth;

    @GetMapping("/schedules")
    public List<Map<String, Object>> getMyGroupsSubjects() {
        Long teacherId = auth.get().id();

        return jdbc.queryForList("""
            SELECT DISTINCT s.group_id, s.subject_id
            FROM schedule s
            WHERE s.teacher_id = ?
        """, teacherId);
    }
}
