package com.fibre.optique.auth.service;

import com.fibre.optique.auth.exception.AuthException;
import com.fibre.optique.auth.model.LoginAuditLog;
import com.fibre.optique.auth.model.RefreshToken;
import com.fibre.optique.auth.repository.LoginAuditLogRepository;
import com.fibre.optique.users.entity.Role;
import com.fibre.optique.users.entity.User;
import com.fibre.optique.users.exception.UserValidationException;
import com.fibre.optique.users.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final LoginAuditLogRepository auditLogRepository;

    public AuthService(UserService userService,
                       TokenService tokenService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       LoginAuditLogRepository auditLogRepository) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.auditLogRepository = auditLogRepository;
    }

    // =========================================================================
    // PUBLIC REGISTRATION — PROSPECT only
    // =========================================================================

    /**
     * Public registration endpoint.
     * Role is ALWAYS forced to PROSPECT — internal staff accounts
     * must be created by an admin via UserService.
     */
    @Transactional
    public Map<String, String> register(String nom, String prenom, String email, String password) {
        if (userService.existsByEmail(email)) {
            throw new UserValidationException("Cet email est déjà utilisé : " + email);
        }

        User user = User.builder()
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.PROSPECT)   // always PROSPECT — no exceptions
                .enabled(true)
                .build();

        User savedUser = userService.save(user);

        return buildTokenMap(savedUser);
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    @Transactional
    public Map<String, String> login(String email, String password, String ipAddress) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new AuthException("Utilisateur non trouvé après authentification."));

            user.setLastLoginAt(Instant.now());
            userService.save(user);

            auditLogRepository.save(LoginAuditLog.builder()
                    .email(email).result("SUCCESS").ipAddress(ipAddress).build());

            return buildTokenMap(user);

        } catch (AuthenticationException e) {
            auditLogRepository.save(LoginAuditLog.builder()
                    .email(email).result("FAILED:" + e.getMessage()).ipAddress(ipAddress).build());
            throw new AuthException("Email ou mot de passe incorrect.");
        }
    }

    // =========================================================================
    // TOKEN OPERATIONS
    // =========================================================================

    @Transactional
    public Map<String, String> refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = tokenService.verifyRefreshToken(refreshTokenStr);
        User user = refreshToken.getUser();

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken",  tokenService.generateToken(user));
        tokens.put("refreshToken", refreshTokenStr);
        return tokens;
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        tokenService.revokeRefreshToken(refreshTokenStr);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Map<String, String> buildTokenMap(User user) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken",  tokenService.generateToken(user));
        tokens.put("refreshToken", tokenService.createRefreshToken(user));
        return tokens;
    }
}
