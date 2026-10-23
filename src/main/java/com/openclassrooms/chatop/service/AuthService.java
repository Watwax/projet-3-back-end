package com.openclassrooms.chatop.service;

import com.openclassrooms.chatop.dto.request.LoginRequest;
import com.openclassrooms.chatop.dto.request.RegisterRequest;
import com.openclassrooms.chatop.dto.response.TokenResponse;
import com.openclassrooms.chatop.dto.response.UserResponse;
import com.openclassrooms.chatop.entity.User;
import com.openclassrooms.chatop.exception.EmailAlreadyRegisteredException;
import com.openclassrooms.chatop.exception.InvalidCredentialsException;
import com.openclassrooms.chatop.exception.UserNotFoundException;
import com.openclassrooms.chatop.repository.UserRepository;
import com.openclassrooms.chatop.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

/** Gère l'inscription, la connexion et la récupération du profil courant. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Crée un compte utilisateur et génère son jeton d'authentification.
     *
     * @param request données nécessaires à l'inscription
     * @return le jeton de l'utilisateur créé
     * @throws EmailAlreadyRegisteredException si l'adresse e-mail est déjà utilisée
     */
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setEmail(email);
        user.setName(request.name().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        User savedUser = userRepository.save(user);

        return new TokenResponse(jwtService.generateToken(savedUser.getEmail()));
    }

    /**
     * Vérifie les identifiants et génère un jeton d'authentification.
     *
     * @param request identifiants de connexion
     * @return le jeton de l'utilisateur authentifié
     * @throws InvalidCredentialsException si les identifiants sont invalides
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            String authenticatedEmail = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password())
            ).getName();
            return new TokenResponse(jwtService.generateToken(authenticatedEmail));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }
    }

    /**
     * Récupère le profil associé à une adresse e-mail.
     *
     * @param email adresse e-mail de l'utilisateur
     * @return le profil utilisateur destiné à l'API
     * @throws UserNotFoundException si aucun utilisateur ne correspond à cette adresse
     */
    @Transactional(readOnly = true)
    public UserResponse getUser(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(UserNotFoundException::new);
        return UserResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
