package org.example.otp.dao;

import org.example.otp.model.User;
import java.util.List;

/**
 * Интерфейс доступа к данным для работы с пользователями
 * Предоставляет методы для создания, поиска и управления учетными записями пользователей
 */
public interface UserDao {


    void create(User user);

    User findByUsername(String username);

    User findById(Long id);


    List<User> findAllUsersWithoutAdmins();


    boolean adminExists();

    void delete(Long userId);
}
