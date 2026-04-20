package ru.didcvee.ecd.security.repository;

import org.springframework.jdbc.core.RowMapper;
import ru.didcvee.ecd.security.repository.model.RefreshToken;
import ru.didcvee.ecd.security.repository.model.Role;
import ru.didcvee.ecd.security.repository.model.User;

public class Mappers {

    public static final RowMapper<User> USER = (rs, i) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFirstname(rs.getString("firstname"));
        u.setMiddlename(rs.getString("middlename"));
        u.setLastname(rs.getString("lastname"));
        u.setIsActive(rs.getBoolean("is_active"));
        return u;
    };

    public static final RowMapper<Role> ROLE = (rs, i) -> {
        Role r = new Role();
        r.setId(rs.getInt("id"));
        r.setName(rs.getString("name"));
        return r;
    };

    public static final RowMapper<RefreshToken> TOKEN = (rs, i) -> {
        RefreshToken t = new RefreshToken();
        t.setId(rs.getLong("id"));
        t.setUserId(rs.getLong("user_id"));
        t.setToken(rs.getString("token"));
        t.setExpiryDate(rs.getTimestamp("expirydate").toInstant());
        return t;
    };
}
