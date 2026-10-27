package ru.mindlex.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.mindlex.common.BusinessException;

import java.util.UUID;

/**
 * Доступ к идентификатору текущего пользователя.
 */
@Component
public class CurrentUser {

    public UUID id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            throw new BusinessException("Требуется аутентификация");
        }
        return userId;
    }
}
