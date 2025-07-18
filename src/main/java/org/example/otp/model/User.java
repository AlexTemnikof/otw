package org.example.otp.model;

import java.util.Objects;

/**
 * Модель данных пользователя системы
 * Содержит информацию для идентификации, аутентификации и авторизации пользователя
 */
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private UserRole role;

    public User() {
    }

    public User(Long id, String username, String passwordHash, UserRole role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return Objects.equals(id, user.id)
                && Objects.equals(username, user.username)
                && Objects.equals(passwordHash, user.passwordHash)
                && role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, passwordHash, role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}
