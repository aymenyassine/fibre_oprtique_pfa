package com.fibre.optique.auth.controller;

import com.fibre.optique.auth.dto.AuthResponse;
import com.fibre.optique.auth.dto.LoginRequest;
import com.fibre.optique.auth.dto.RefreshTokenRequest;
import com.fibre.optique.auth.dto.RegisterRequest;
import com.fibre.optique.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Public self-registration — creates a PROSPECT account only.
     * <p>
     * Internal staff accounts (ADMIN, COMMERCIAL, TECHNICIEN, SUPPORT)
     * must be created by an ADMIN via {@code POST /api/v1/users}.
     * </p>
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, String> tokens = authService.register(
                request.getNom(),
                request.getPrenom(),
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.builder()
                        .accessToken(tokens.get("accessToken"))
                        .refreshToken(tokens.get("refreshToken"))
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {

        Map<String, String> tokens = authService.login(
                request.getEmail(),
                request.getPassword(),
                servletRequest.getRemoteAddr()
        );
        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        Map<String, String> tokens = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
