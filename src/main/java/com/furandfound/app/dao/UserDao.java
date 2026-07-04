package com.furandfound.app.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.furandfound.app.model.User;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findByEmail(String email) {
        String sql = "SELECT u.id, u.full_name, u.email, u.password_hash, u.phone, r.name as role FROM users u JOIN roles r ON u.role_id = r.id WHERE u.email = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), email);
        return users.isEmpty() ? null : users.get(0);
    }

    public User findByPhone(String phone) {
        String sql = "SELECT u.id, u.full_name, u.email, u.password_hash, u.phone, r.name as role FROM users u JOIN roles r ON u.role_id = r.id WHERE u.phone = ? ORDER BY u.id DESC";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), phone);
        return users.isEmpty() ? null : users.get(0);
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    public void save(User user) {
        jdbcTemplate.update(
                "INSERT INTO users(full_name, email, password_hash, phone, role_id) VALUES (?, ?, ?, ?, (SELECT id FROM roles WHERE name = ?))",
                user.getFullName(), user.getEmail(), user.getPasswordHash(), user.getPhone(), user.getRole()
        );
    }

    public User findOrCreateCustomer(String fullName, String email, String phone, String passwordHash) {
        User existing = email != null && !email.isBlank() ? findByEmail(email) : null;
        if (existing == null && phone != null && !phone.isBlank()) {
            existing = findByPhone(phone);
        }
        if (existing != null) {
            return existing;
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordHash);
        user.setRole("CUSTOMER");
        save(user);
        return findByEmail(email);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setPhone(rs.getString("phone"));
            user.setRole(rs.getString("role"));
            return user;
        }
    }
}
