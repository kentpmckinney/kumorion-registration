package org.kumoricon.registration.model.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


@Service
public class UserRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public User findOneByUsernameIgnoreCase(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT users.*, roles.name as rolename from users join roles on users.role_id = roles.id WHERE users.username=:username",
                    Map.of("username", username), new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    User findOneById(Integer id) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT users.*, roles.name as rolename from users join roles on users.role_id = roles.id where users.id=:id",
                    Map.of("id", id), new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional
    public void save(User user) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        if (user.getId() == null) {
            jdbcTemplate.update("INSERT INTO users " +
                            "(account_non_expired, account_non_locked, force_password_change, enabled, first_name, " +
                            "last_name, last_badge_number_created, password, username, role_id) " +
                            "VALUES(:accountNonExpired, :accountNonLocked, :forcePasswordChange, :enabled, :firstName," +
                            ":lastName, :lastBadgeNumberCreated, :password, :username, :roleId)",
                    params);
        } else {
            jdbcTemplate.update("UPDATE users SET account_non_expired = :accountNonExpired, " +
                            "account_non_locked = :accountNonLocked, force_password_change = :forcePasswordChange, " +
                            "enabled = :enabled, first_name = :firstName, last_name = :lastName, " +
                            "last_badge_number_created = :lastBadgeNumberCreated, password = :password, " +
                            "username = :username, role_id = :roleId WHERE id = :id",
                    params);
        }
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT users.*, roles.name as rolename from users join roles on users.role_id = roles.id;",
                new UserRowMapper());
    }

    public Integer count() {
        String sql = "select count(*) from users";
        return jdbcTemplate.queryForObject(sql, Map.of(), Integer.class);
    }

    @Transactional
    public void incrementBadgeNumberForUser(String username) {
        final String sql = "update users set last_badge_number_created = last_badge_number_created + 1 where users.username=:username";
        jdbcTemplate.update(sql, Map.of("username", username));
    }

    @Transactional
    public void setPassword(Integer userId, boolean forcePasswordChange, String password) {
        int rowsUpdated = jdbcTemplate.update("UPDATE users SET password = :password, force_password_change = :forcePasswordChange  WHERE id = :userId",
                Map.of("password", password,
                        "forcePasswordChange", forcePasswordChange,
                        "userId", userId));
        if (rowsUpdated < 1) {
            throw new RuntimeException("No rows updated when updating password for user id " + userId);
        } else if (rowsUpdated > 1) {
            throw new RuntimeException(rowsUpdated + " rows updated when updating for user id " + userId + ". Should be 1");
        }
    }

    static class UserRowMapper implements RowMapper<User>
    {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setForcePasswordChange(rs.getBoolean("force_password_change"));
            user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
            user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
            user.setEnabled(rs.getBoolean("enabled"));
            user.setLastBadgeNumberCreated(rs.getInt("last_badge_number_created"));
            user.setRoleId(rs.getInt("role_id"));
            user.setRoleName(rs.getString("rolename"));
            return user;
        }
    }
}
