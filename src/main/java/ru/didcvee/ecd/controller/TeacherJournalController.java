package ru.didcvee.ecd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.didcvee.ecd.service.CurrentUserProvider;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/teacher/journal")
@RequiredArgsConstructor
@Transactional
public class TeacherJournalController {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUserProvider auth;

    @GetMapping("/groups")
    public List<Map<String, Object>> groups() {

        Long teacherId = auth.get().id();

        return jdbcTemplate.queryForList("""
            select distinct
                g.id,
                g.name
            from schedule s
            join groups g on g.id = s.group_id
            where s.teacher_id = ?
            order by g.name
            """,
                teacherId
        );
    }

    @GetMapping("/subjects")
    public List<Map<String, Object>> subjects(
            @RequestParam Integer groupId
    ) {

        Long teacherId = auth.get().id();;

        return jdbcTemplate.queryForList("""
            select distinct
                s2.id,
                s2.name
            from schedule s
            join subjects s2 on s2.id = s.subject_id
            where s.teacher_id = ?
              and s.group_id = ?
            order by s2.name
            """,
                teacherId,
                groupId
        );
    }

    @GetMapping("/lessons")
    public List<Map<String, Object>> lessons(
            @RequestParam Integer groupId,
            @RequestParam Integer subjectId,
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo
    ) {

        Long teacherId = auth.get().id();;

        return jdbcTemplate.queryForList("""
            select
                id,
                date,
                lesson_number
            from schedule
            where teacher_id = ?
              and group_id = ?
              and subject_id = ?
              and date between ? and ?
            order by date, lesson_number
            """,
                teacherId,
                groupId,
                subjectId,
                dateFrom,
                dateTo
        );
    }

    @GetMapping
    public Map<String, Object> journal(
            @RequestParam Integer groupId,
            @RequestParam Integer subjectId,
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo
    ) {

        Long teacherId = auth.get().id();;

        List<Map<String, Object>> lessons = jdbcTemplate.queryForList("""
            select
                id,
                date,
                lesson_number
            from schedule
            where teacher_id = ?
              and group_id = ?
              and subject_id = ?
              and date between ? and ?
            order by date, lesson_number
            """,
                teacherId,
                groupId,
                subjectId,
                dateFrom,
                dateTo
        );

        List<Map<String, Object>> students = jdbcTemplate.queryForList("""
            select
                id,
                lastname,
                firstname,
                middlename
            from users
            where group_id = ?
            order by lastname, firstname, middlename
            """,
                groupId
        );

        List<Map<String, Object>> grades = jdbcTemplate.queryForList("""
            select
                g.student_id,
                g.schedule_id,
                g.value,
                g.comment
            from grades g
            join schedule s on s.id = g.schedule_id
            where s.teacher_id = ?
              and s.group_id = ?
              and s.subject_id = ?
              and s.date between ? and ?
            """,
                teacherId,
                groupId,
                subjectId,
                dateFrom,
                dateTo
        );

        Map<Long, Map<Long, Integer>> gradeMap = new HashMap<>();

        for (Map<String, Object> row : grades) {

            Long studentId =
                    ((Number) row.get("student_id")).longValue();

            Long lessonId =
                    ((Number) row.get("schedule_id")).longValue();

            Integer value =
                    ((Number) row.get("value")).intValue();

            gradeMap
                    .computeIfAbsent(studentId, k -> new HashMap<>())
                    .put(lessonId, value);
        }

        List<Map<String, Object>> resultStudents = new ArrayList<>();

        for (Map<String, Object> student : students) {

            Long studentId =
                    ((Number) student.get("id")).longValue();

            String fullName =
                    Objects.toString(student.get("lastname"), "") + " "
                            + Objects.toString(student.get("firstname"), "") + " "
                            + Objects.toString(student.get("middlename"), "");

            Map<String, Object> row = new HashMap<>();

            row.put("id", studentId);
            row.put("name", fullName.trim());
            row.put(
                    "grades",
                    gradeMap.getOrDefault(studentId, Collections.emptyMap())
            );

            resultStudents.add(row);
        }

        Map<String, Object> response = new HashMap<>();

        response.put("lessons", lessons);
        response.put("students", resultStudents);

        return response;
    }

    @PostMapping("/grade")
    public void saveGrade(
            @RequestBody SaveGradeRequest request
    ) {

        jdbcTemplate.update("""
            insert into grades(
                student_id,
                schedule_id,
                value,
                comment
            )
            values (?, ?, ?, ?)
            on conflict(student_id, schedule_id)
            do update set
                value = excluded.value,
                comment = excluded.comment
            """,
                request.studentId(),
                request.lessonId(),
                request.value(),
                request.comment()
        );
    }

    public record SaveGradeRequest(
            Long studentId,
            Long lessonId,
            Integer value,
            String comment
    ) {
    }

    @DeleteMapping("/grade/{studentId}/{lessonId}")
    public void deleteGrade(
            @PathVariable Long studentId,
            @PathVariable Long lessonId
    ) {

        jdbcTemplate.update("""
        delete from grades
        where student_id = ?
          and schedule_id = ?
        """,
                studentId,
                lessonId
        );
    }
}