package ru.mindlex.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.common.ConflictException;
import ru.mindlex.deck.Deck;
import ru.mindlex.deck.DeckRepository;
import ru.mindlex.security.JwtService;
import ru.mindlex.settings.UserSettings;
import ru.mindlex.settings.UserSettingsRepository;
import ru.mindlex.user.User;
import ru.mindlex.user.UserRepository;
import ru.mindlex.user.UserResponse;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserSettingsRepository settingsRepository;
    private final DeckRepository deckRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       UserSettingsRepository settingsRepository,
                       DeckRepository deckRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.deckRepository = deckRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Пользователь с таким e-mail уже зарегистрирован");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        user.setRole("ROLE_USER");
        userRepository.save(user);

        String language = request.language() == null || request.language().isBlank()
                ? "en"
                : request.language().trim().toLowerCase(Locale.ROOT);

        Deck personalDeck = new Deck();
        personalDeck.setOwnerId(user.getId());
        personalDeck.setName("Мой словарь");
        personalDeck.setLanguage(language);
        personalDeck.setDescription("Личная колода пользователя: сюда попадают добавленные вручную слова");
        personalDeck.setPublicDeck(false);
        deckRepository.save(personalDeck);

        UserSettings settings = UserSettings.defaultsFor(user.getId());
        settings.setLanguage(language);
        settings.setSelectedDeckId(personalDeck.getId());
        settingsRepository.save(settings);

        log.info("Зарегистрирован пользователь {}", user.getId());
        return issue(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Пользователь не найден"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Неверный пароль");
        }
        return issue(user);
    }

    private AuthResponse issue(User user) {
        return AuthResponse.of(jwtService.issue(user), jwtService.expiresInSeconds(), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(UUID userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new BadCredentialsException("Пользователь не найден"));
    }
}
