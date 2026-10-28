package ru.mindlex.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mindlex.security.CurrentUser;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Пользователи", description = "Профиль текущего пользователя")
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    @Operation(summary = "Профиль текущего пользователя")
    public UserResponse me() {
        return userService.get(currentUser.id());
    }

    @PutMapping("/me")
    @Operation(summary = "Обновить профиль")
    public UserResponse update(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(currentUser.id(), request);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Удалить аккаунт со всеми данными")
    public ResponseEntity<Void> delete() {
        userService.deleteAccount(currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
