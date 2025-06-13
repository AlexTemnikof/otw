package org.example.otp.dao;

import org.example.otp.config.DatabaseManager;
import org.example.otp.model.User;
import org.example.otp.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Database access implementation for user management operations
 * Provides database interaction for storing and managing user accounts
 */
public class UserDaoImpl implements UserDao {
    // Logger for this class
    private static final Logger LOG = LoggerFactory.getLogger(UserDaoImpl.class);

    // Database table and column names
    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD_HASH = "password_hash";
    private static final String COL_ROLE = "role";

    // SQL query templates
    private static final String SQL_INSERT_USER = 
            "INSERT INTO " + TABLE_USERS + " (" + COL_USERNAME + ", " + COL_PASSWORD_HASH + ", " + COL_ROLE + ") VALUES (?, ?, ?)";
    private static final String SQL_FIND_BY_USERNAME = 
            "SELECT " + COL_ID + ", " + COL_USERNAME + ", " + COL_PASSWORD_HASH + ", " + COL_ROLE + 
            " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?";
    private static final String SQL_FIND_BY_ID = 
            "SELECT " + COL_ID + ", " + COL_USERNAME + ", " + COL_PASSWORD_HASH + ", " + COL_ROLE + 
            " FROM " + TABLE_USERS + " WHERE " + COL_ID + " = ?";
    private static final String SQL_FIND_NON_ADMIN_USERS = 
            "SELECT " + COL_ID + ", " + COL_USERNAME + ", " + COL_PASSWORD_HASH + ", " + COL_ROLE + 
            " FROM " + TABLE_USERS + " WHERE " + COL_ROLE + " <> 'ADMIN'";
    private static final String SQL_CHECK_ADMIN_EXISTS = 
            "SELECT 1 FROM " + TABLE_USERS + " WHERE " + COL_ROLE + " = 'ADMIN' LIMIT 1";
    private static final String SQL_DELETE_USER = 
            "DELETE FROM " + TABLE_USERS + " WHERE " + COL_ID + " = ?";

    /**
     * Creates a new user in the database
     * 
     * @param user the user to create
     * @throws RuntimeException if database operation fails
     */
    @Override
    public void create(User user) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());

            // Execute insert
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("User creation failed, no rows affected");
            }

            // Get generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                }
            }

            LOG.info("Successfully created user: {}", user);
        } catch (SQLException e) {
            LOG.error("Database error while creating user [{}]: {}", user.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * Finds a user by their username
     * 
     * @param username the username to search for
     * @return the user or null if not found
     * @throws RuntimeException if database operation fails
     */
    @Override
    public User findByUsername(String username) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_USERNAME)) {

            stmt.setString(1, username);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    User user = mapRow(resultSet);
                    LOG.info("Found user with username {}", username);
                    return user;
                }
            }
        } catch (SQLException e) {
            LOG.error("Database error while finding user by username [{}]: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to find user by username", e);
        }

        LOG.debug("No user found with username: {}", username);
        return null;
    }

    /**
     * Finds a user by their ID
     * 
     * @param id the user ID to search for
     * @return the user or null if not found
     * @throws RuntimeException if database operation fails
     */
    @Override
    public User findById(Long id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setLong(1, id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    User user = mapRow(resultSet);
                    LOG.info("Found user with ID {}", id);
                    return user;
                }
            }
        } catch (SQLException e) {
            LOG.error("Database error while finding user by ID [{}]: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to find user by ID", e);
        }

        LOG.debug("No user found with ID: {}", id);
        return null;
    }

    /**
     * Retrieves all non-admin users from the database
     * 
     * @return list of non-admin users
     * @throws RuntimeException if database operation fails
     */
    @Override
    public List<User> findAllUsersWithoutAdmins() {
        List<User> userList = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_NON_ADMIN_USERS);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                userList.add(mapRow(resultSet));
            }

            LOG.info("Retrieved {} non-admin users", userList.size());
        } catch (SQLException e) {
            LOG.error("Database error while retrieving non-admin users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve non-admin users", e);
        }

        return userList;
    }

    /**
     * Checks if any admin users exist in the database
     * 
     * @return true if at least one admin exists, false otherwise
     * @throws RuntimeException if database operation fails
     */
    @Override
    public boolean adminExists() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_CHECK_ADMIN_EXISTS);
             ResultSet resultSet = stmt.executeQuery()) {

            boolean hasAdmin = resultSet.next();
            LOG.info("Admin user exists: {}", hasAdmin);
            return hasAdmin;
        } catch (SQLException e) {
            LOG.error("Database error while checking for admin existence: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check for admin existence", e);
        }
    }

    /**
     * Deletes a user from the database by their ID
     * 
     * @param userId the ID of the user to delete
     * @throws RuntimeException if database operation fails
     */
    @Override
    public void delete(Long userId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_USER)) {

            stmt.setLong(1, userId);
            int rowsAffected = stmt.executeUpdate();

            LOG.info("Deleted user with ID {}: {} rows affected", userId, rowsAffected);
        } catch (SQLException e) {
            LOG.error("Database error while deleting user [{}]: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        return user;
    }
}
