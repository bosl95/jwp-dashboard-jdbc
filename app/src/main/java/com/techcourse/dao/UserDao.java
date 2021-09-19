package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserDao {
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final String QUERY_LOG = "query : {}";

    private static final String ID = "id";
    private static final String ACCOUNT = "account";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        jdbcTemplate.update(conn -> {
//            try (PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"id"})) {
//                pstmt.setString(1, user.getAccount());
//                pstmt.setString(2, user.getPassword());
//                pstmt.setString(3, user.getEmail());
//                return pstmt;
//            }
//        }, keyHolder);
//        Number keyValue = keyHolder.getKey();
//        log.debug("Number {} insert success ", keyValue.longValue());

        InsertJdbcTemplate insertJdbcTemplate = new InsertJdbcTemplate();
        insertJdbcTemplate.insert(user, this);
    }

    public void update(User user) {
        UpdateJdbcTemplate updateJdbcTemplate = new UpdateJdbcTemplate();
        updateJdbcTemplate.update(user, this);
    }

    public List<User> findAll() {
        final String sql = "select * from users";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug(QUERY_LOG, sql);

            List<User> users = new ArrayList<>();
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                long id = rs.getLong(ID);
                String account = rs.getString(ACCOUNT);
                String password = rs.getString(PASSWORD);
                String email = rs.getString(EMAIL);
                User user = new User(id, account, password, email);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            handleUserDaoException(e);
        }
        return Collections.emptyList();
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug(QUERY_LOG, sql);

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getLong(ID),
                        rs.getString(ACCOUNT),
                        rs.getString(PASSWORD),
                        rs.getString(EMAIL));
            }
        } catch (SQLException e) {
            handleUserDaoException(e);
        }
        return null;
    }

    public User findByAccount(String account) {
        final String sql = "select * from users where account = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug(QUERY_LOG, sql);

            pstmt.setString(1, account);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(ID);
                String password = rs.getString(PASSWORD);
                String email = rs.getString(EMAIL);
                return new User(id, account, password, email);
            }
        } catch (SQLException e) {
            handleUserDaoException(e);
        }
        return null;
    }

    private void handleUserDaoException(final SQLException e) {
        log.error(e.getMessage(), e);
        throw new UserDaoException(e);
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
}
