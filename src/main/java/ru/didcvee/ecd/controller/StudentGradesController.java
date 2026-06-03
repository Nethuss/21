package ru.didcvee.ecd.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import ru.didcvee.ecd.service.CurrentUserProvider;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentGradesController {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUserProvider auth;

    @GetMapping("/gradess")
    public Map<String, Object> grades(
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo
    ) {

        Long studentId = auth.get().id();

        List<Map<String, Object>> lessons =
                jdbcTemplate.queryForList("""
                    select distinct
                        s.id,
                        s.date,
                        s.lesson_number
                    from schedule s
                    join grades g on g.schedule_id = s.id
                    where g.student_id = ?
                      and s.date between ? and ?
                    order by s.date, s.lesson_number
                    """,
                        studentId,
                        dateFrom,
                        dateTo
                );

        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList("""
                    select
                        sub.id as subject_id,
                        sub.name as subject_name,
                        sch.id as lesson_id,
                        g.value
                    from grades g
                    join schedule sch
                        on sch.id = g.schedule_id
                    join subjects sub
                        on sub.id = sch.subject_id
                    where g.student_id = ?
                      and sch.date between ? and ?
                    order by sub.name, sch.date
                    """,
                        studentId,
                        dateFrom,
                        dateTo
                );

        Map<Long, Map<String, Object>> subjectsMap =
                new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {

            Long subjectId =
                    ((Number) row.get("subject_id")).longValue();

            String subjectName =
                    Objects.toString(row.get("subject_name"));

            Long lessonId =
                    ((Number) row.get("lesson_id")).longValue();

            Object value = row.get("value");

            Map<String, Object> subject =
                    subjectsMap.computeIfAbsent(subjectId, k -> {

                        Map<String, Object> map = new HashMap<>();

                        map.put("id", subjectId);
                        map.put("name", subjectName);
                        map.put("grades", new HashMap<Long, Object>());

                        return map;
                    });

            Map<Long, Object> grades =
                    (Map<Long, Object>) subject.get("grades");

            grades.put(lessonId, value);
        }

        Map<String, Object> response =
                new HashMap<>();

        response.put("lessons", lessons);
        response.put(
                "subjects",
                new ArrayList<>(subjectsMap.values())
        );

        return response;
    }

    @GetMapping("/statistics")
    public Map<String, Object> statistics(
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo
    ) {

        Long studentId = auth.get().id();

        List<Map<String, Object>> averageTrend =
                jdbcTemplate.queryForList("""
                    select
                        to_char(
                            date_trunc('month', s.date),
                            'YYYY-MM'
                        ) as period,

                        round(
                            avg(g.value)::numeric,
                            2
                        ) as average

                    from grades g

                    join schedule s
                        on s.id = g.schedule_id

                    where g.student_id = ?
                      and s.date between ? and ?
                      and g.value between 2 and 5

                    group by
                        date_trunc('month', s.date)

                    order by
                        date_trunc('month', s.date)
                    """,
                        studentId,
                        dateFrom,
                        dateTo
                );

        List<Map<String, Object>> distribution =
                jdbcTemplate.queryForList("""
                    select
                        g.value as grade,
                        count(*) as count

                    from grades g

                    join schedule s
                        on s.id = g.schedule_id

                    where g.student_id = ?
                      and s.date between ? and ?
                      and g.value between 2 and 5

                    group by g.value

                    order by g.value desc
                    """,
                        studentId,
                        dateFrom,
                        dateTo
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put("averageTrend", averageTrend);
        response.put("distribution", distribution);

        return response;
    }
}
