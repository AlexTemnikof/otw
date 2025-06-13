package org.example.otp.service;

import org.example.otp.dao.UserDao;
import org.example.otp.model.User;
import org.example.otp.model.UserRole;
import org.example.otp.util.PasswordEncoder;
import org.example.otp.util.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Сервис для управления пользователями
 * Предоставляет функциональность для регистрации, аутентификации и управления учетными записями
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void register(String username, String password, UserRole role) {
        if (userDao.findByUsername(username) != null) {
            logger.warn("Attempt to register with existing username: {}", username);
            throw new IllegalArgumentException("Username already exists");
        }
        if (role == UserRole.ADMIN && adminExists()) {
            logger.warn("Attempt to register second ADMIN: {}", username);
            throw new IllegalStateException("Administrator already exists");
        }

        String hashed = PasswordEncoder.hash(password);
        User user = new User(null, username, hashed, role);
        userDao.create(user);
        logger.info("Registered new user: {} with role {}", username, role);
    }

    public boolean adminExists() {
        List<User> users = userDao.findAllUsersWithoutAdmins();
        return users.isEmpty();
    }

    public String login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            logger.warn("Login failed: user not found {}", username);
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (!PasswordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Login failed: wrong password for {}", username);
            throw new IllegalArgumentException("Invalid username or password");
        }
        String token = TokenManager.generateToken(user);
        logger.info("User {} logged in, token generated", username);
        return token;
    }

    public User findById(Long id) {
        return userDao.findById(id);
    }

    public List<User> findAllWithoutAdmins() {
        return userDao.findAllUsersWithoutAdmins();
    }

    public void deleteUser(Long id) {
        userDao.delete(id);
        logger.info("Deleted user with id {}", id);
    }
}
