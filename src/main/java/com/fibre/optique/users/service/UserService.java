package com.fibre.optique.users.service;

import com.fibre.optique.users.dto.UserCreateRequest;
import com.fibre.optique.users.dto.UserDto;
import com.fibre.optique.users.dto.UserUpdateRequest;
import com.fibre.optique.users.entity.Role;
import com.fibre.optique.users.entity.User;
import com.fibre.optique.users.exception.UserNotFoundException;
import com.fibre.optique.users.exception.UserValidationException;
import com.fibre.optique.users.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // @Lazy breaks the circular dependency: SecurityConfig → PasswordEncoder → UserService
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================================
    // RAW ENTITY ACCESS — used by AuthService, CustomUserDetailsService, etc.
    // =========================================================================

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /** Persists any User entity directly — used by AuthService on login (lastLoginAt update). */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    // =========================================================================
    // DTO-BASED CRUD — used by UserController
    // =========================================================================

    public Page<UserDto> searchUsers(String query, Role role, Boolean enabled, Pageable pageable) {
        return userRepository.searchUsers(query, role, enabled, pageable)
                .map(UserDto::fromEntity);
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDto::fromEntity)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserValidationException("Cet email est déjà utilisé : " + request.getEmail());
        }

        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.PROSPECT)
                .enabled(true)
                .build();

        return UserDto.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new UserValidationException(
                    "Cet email est déjà utilisé par un autre utilisateur : " + request.getEmail());
        }

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());

        if (request.getRole() != null)    user.setRole(request.getRole());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());

        return UserDto.fromEntity(userRepository.save(user));
    }

    /** Soft-delete: disables the user account without removing the record. */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setEnabled(false);
        userRepository.save(user);
    }
}
