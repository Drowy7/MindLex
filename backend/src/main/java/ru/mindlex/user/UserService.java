package ru.mindlex.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.common.NotFoundException;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User require(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("Пользователь", userId));
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID userId) {
        return UserResponse.from(require(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = require(userId);
        user.setDisplayName(request.displayName().trim());
        return UserResponse.from(userRepository.save(user));
    }

    /** Удаление аккаунта вместе со связанными данными (каскад настроен в схеме БД). */
    @Transactional
    public void deleteAccount(UUID userId) {
        User user = require(userId);
        userRepository.delete(user);
    }
}
