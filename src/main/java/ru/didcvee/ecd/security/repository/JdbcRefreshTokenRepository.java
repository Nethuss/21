package ru.didcvee.ecd.security.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.didcvee.ecd.security.repository.model.RefreshToken;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcRefreshTokenRepository {

    private final JdbcTemplate jdbc;

    public Optional<RefreshToken> findByToken(String token) {
        var list = jdbc.query(
                "SELECT * FROM refreshtoken WHERE token = ?",
                Mappers.TOKEN,
                token
        );
        return list.stream().findFirst();
    }

    public Optional<RefreshToken> findByUserId(Long userId) {
        var list = jdbc.query(
                "SELECT * FROM refreshtoken WHERE user_id = ?",
                Mappers.TOKEN,
                userId
        );
        return list.stream().findFirst();
    }

    public void save(RefreshToken token) {
        jdbc.update("""
            INSERT INTO refreshtoken(user_id, token, expirydate)
            VALUES (?, ?, ?)
        """,
                token.getUserId(),
                token.getToken(),
                java.sql.Timestamp.from(token.getExpiryDate())
        );
    }

    public void deleteByUserId(Long userId) {
        jdbc.update("DELETE FROM refreshtoken WHERE user_id = ?", userId);
    }

    public void deleteByUsername(String username) {
        jdbc.update("""
            DELETE FROM refreshtoken
            WHERE user_id = (SELECT id FROM users WHERE username = ?)
        """, username);
    }
}