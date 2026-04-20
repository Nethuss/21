package ru.didcvee.ecd.security.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.didcvee.ecd.security.repository.model.Role;
import ru.didcvee.ecd.security.repository.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcUserRepository {

    private final JdbcTemplate jdbc;

    public Optional<User> findByUsername(String username) {
        var list = jdbc.query(
                "SELECT * FROM users WHERE username = ?",
                Mappers.USER,
                username
        );
        return list.stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        var list = jdbc.query(
                "SELECT * FROM users WHERE id = ?",
                Mappers.USER,
                id
        );
        return list.stream().findFirst();
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    public Optional<User> findByUsernameWithRoles(String username) {

        var rows = jdbc.query("""
        SELECT 
            u.id as u_id,
            u.username,
            u.password,
            u.firstname,
            u.middlename,
            u.lastname,
            u.is_active,

            r.id as r_id,
            r.name as r_name
        FROM users u
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        LEFT JOIN role r ON r.id = ur.role_id
        WHERE u.username = ?
    """, rs -> {

            User user = null;

            while (rs.next()) {
                if (user == null) {
                    user = new User();
                    user.setId(rs.getLong("u_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setFirstname(rs.getString("firstname"));
                    user.setMiddlename(rs.getString("middlename"));
                    user.setLastname(rs.getString("lastname"));
                    user.setIsActive(rs.getBoolean("is_active"));
                    user.setRoles(new ArrayList<>());
                }

                int roleId = rs.getInt("r_id");
                if (!rs.wasNull()) {
                    Role role = new Role();
                    role.setId(roleId);
                    role.setName(rs.getString("r_name"));

                    user.getRoles().add(role);
                }
            }

            return user;
        }, username);

        return Optional.ofNullable(rows);
    }

    public void save(User user) {
        jdbc.update("""
            INSERT INTO users(username, password, firstname, middlename, lastname, is_active)
            VALUES (?, ?, ?, ?, ?, ?)
        """,
                user.getUsername(),
                user.getPassword(),
                user.getFirstname(),
                user.getMiddlename(),
                user.getLastname(),
                user.getIsActive()
        );
    }

    public void deactivateById(Long id) {
        jdbc.update("UPDATE users SET is_active = false WHERE id = ?", id);
    }

    public void deactivateByUsername(String username) {
        jdbc.update("UPDATE users SET is_active = false WHERE username = ?", username);
    }

    public List<Role> getRoles(Long userId) {
        return jdbc.query("""
            SELECT r.* FROM role r
            JOIN user_roles ur ON r.id = ur.role_id
            WHERE ur.user_id = ?
        """, Mappers.ROLE, userId);
    }
}
