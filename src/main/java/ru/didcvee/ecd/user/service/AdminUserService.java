package ru.didcvee.ecd.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.didcvee.ecd.user.dto.CreateUserRequest;
import ru.didcvee.ecd.user.dto.SubjectDto;
import ru.didcvee.ecd.user.dto.UpdateUserRequest;
import ru.didcvee.ecd.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    // 📌 GET ALL
    public List<UserDto> getAll() {
        return jdbc.query("SELECT * FROM users", (rs, i) -> {
            UserDto u = new UserDto();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setFirstname(rs.getString("firstname"));
            u.setMiddlename(rs.getString("middlename"));
            u.setLastname(rs.getString("lastname"));
            u.setIsActive(rs.getBoolean("is_active"));
            u.setGroupId(rs.getObject("group_id") != null ? rs.getLong("group_id") : null);

            var roles = jdbc.query("""
                SELECT r.name FROM role r
                JOIN user_roles ur ON r.id = ur.role_id
                WHERE ur.user_id = ?
            """, (r, j) -> r.getString("name"), u.getId());

            u.setRoles(roles);

            var subjects = jdbc.query("""
    SELECT s.id, s.name FROM subjects s
    JOIN teacher_subjects ts ON s.id = ts.subject_id
    WHERE ts.teacher_id = ?
""", (r, j) -> {
                SubjectDto s = new SubjectDto();
                s.setId(r.getLong("id"));
                s.setName(r.getString("name"));
                return s;
            }, u.getId());

            u.setSubjects(subjects);
            return u;
        });
    }

    // 📌 CREATE
    public void create(CreateUserRequest req) {
        String hashed = encoder.encode(req.getPassword());

        jdbc.update("""
            INSERT INTO users(username, password, firstname, middlename, lastname, is_active, group_id)
            VALUES (?, ?, ?, ?, ?, true, ?)
        """,
                req.getUsername(),
                hashed,
                req.getFirstname(),
                req.getMiddlename(),
                req.getLastname(),
                req.getGroupId()
        );

        Long userId = jdbc.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                Long.class,
                req.getUsername()
        );

        if (req.getRoles() != null) {
            for (String role : req.getRoles()) {
                jdbc.update("""
                    INSERT INTO user_roles(user_id, role_id)
                    SELECT ?, id FROM role WHERE name = ?
                """, userId, role);
            }
        }

        if (req.getSubjectIds() != null) {
            for (Long subjectId : req.getSubjectIds()) {
                jdbc.update("""
            INSERT INTO teacher_subjects(teacher_id, subject_id)
            VALUES (?, ?)
        """, userId, subjectId);
            }
        }
    }

    // 📌 UPDATE
    public void update(Long id, UpdateUserRequest req) {

        if (req.getPassword() != null) {
            jdbc.update("UPDATE users SET password = ? WHERE id = ?",
                    encoder.encode(req.getPassword()), id);
        }

        jdbc.update("""
            UPDATE users
            SET firstname = ?, middlename = ?, lastname = ?, is_active = ?, group_id = ?
            WHERE id = ?
        """,
                req.getFirstname(),
                req.getMiddlename(),
                req.getLastname(),
                req.getIsActive(),
                req.getGroupId(),
                id
        );

        // роли — тупо пересоздаем (MVP стиль 😄)
        jdbc.update("DELETE FROM user_roles WHERE user_id = ?", id);

        if (req.getRoles() != null) {
            for (String role : req.getRoles()) {
                jdbc.update("""
                    INSERT INTO user_roles(user_id, role_id)
                    SELECT ?, id FROM role WHERE name = ?
                """, id, role);
            }
        }

        jdbc.update("DELETE FROM teacher_subjects WHERE teacher_id = ?", id);

        if (req.getSubjectIds() != null) {
            for (Long subjectId : req.getSubjectIds()) {
                jdbc.update("""
            INSERT INTO teacher_subjects(teacher_id, subject_id)
            VALUES (?, ?)
        """, id, subjectId);
            }
        }
    }

    // 📌 DELETE
    public void delete(Long id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }

    // 📌 GET ONE
    public UserDto getOne(Long id) {
        return jdbc.queryForObject("""
            SELECT * FROM users WHERE id = ?
        """, (rs, i) -> {
            UserDto u = new UserDto();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setFirstname(rs.getString("firstname"));
            u.setMiddlename(rs.getString("middlename"));
            u.setLastname(rs.getString("lastname"));
            u.setIsActive(rs.getBoolean("is_active"));
            u.setGroupId(rs.getObject("group_id") != null ? rs.getLong("group_id") : null);

            var roles = jdbc.query("""
                SELECT r.name FROM role r
                JOIN user_roles ur ON r.id = ur.role_id
                WHERE ur.user_id = ?
            """, (r, j) -> r.getString("name"), id);

            u.setRoles(roles);

            var subjects = jdbc.query("""
                SELECT s.id, s.name FROM subjects s
                JOIN teacher_subjects ts ON s.id = ts.subject_id
                WHERE ts.teacher_id = ?
            """, (r, j) -> {
                SubjectDto s = new SubjectDto();
                s.setId(r.getLong("id"));
                s.setName(r.getString("name"));
                return s;
            }, id);

            u.setSubjects(subjects);
            return u;
        }, id);
    }
}
