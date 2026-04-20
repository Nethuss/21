package ru.didcvee.ecd.security.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.didcvee.ecd.security.repository.model.Role;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcRoleRepository {

    private final JdbcTemplate jdbc;

    public Optional<Role> findByName(String name) {
        var list = jdbc.query(
                "SELECT * FROM role WHERE name = ?",
                Mappers.ROLE,
                name
        );
        return list.stream().findFirst();
    }

    public List<Role> findAllByNames(List<String> names) {
        String inSql = String.join(",", names.stream().map(n -> "?").toList());

        return jdbc.query(
                "SELECT * FROM role WHERE name IN (" + inSql + ")",
                Mappers.ROLE,
                names.toArray()
        );
    }
}