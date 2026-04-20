package ru.didcvee.ecd.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.didcvee.ecd.user.dto.CreateUserRequest;
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

            var roles = jdbc.query("""
                SELECT r.name FROM role r
                JOIN user_roles ur ON r.id = ur.role_id
                WHERE ur.user_id = ?
            """, (r, j) -> r.getString("name"), u.getId());

            u.setRoles(roles);
            return u;
        });
    }

    // 📌 CREATE
    public void create(CreateUserRequest req) {
        String hashed = encoder.encode(req.getPassword());

        jdbc.update("""
            INSERT INTO users(username, password, firstname, middlename, lastname, is_active)
            VALUES (?, ?, ?, ?, ?, true)
        """,
                req.getUsername(),
                hashed,
                req.getFirstname(),
                req.getMiddlename(),
                req.getLastname()
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
    }

    // 📌 UPDATE
    public void update(Long id, UpdateUserRequest req) {

        if (req.getPassword() != null) {
            jdbc.update("UPDATE users SET password = ? WHERE id = ?",
                    encoder.encode(req.getPassword()), id);
        }

        jdbc.update("""
            UPDATE users
            SET firstname = ?, middlename = ?, lastname = ?, is_active = ?
            WHERE id = ?
        """,
                req.getFirstname(),
                req.getMiddlename(),
                req.getLastname(),
                req.getIsActive(),
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

            var roles = jdbc.query("""
                SELECT r.name FROM role r
                JOIN user_roles ur ON r.id = ur.role_id
                WHERE ur.user_id = ?
            """, (r, j) -> r.getString("name"), id);

            u.setRoles(roles);
            return u;
        }, id);
    }
}
