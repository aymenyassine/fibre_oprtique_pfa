package com.fibre.optique.users.controller;

import com.fibre.optique.users.dto.UserCreateRequest;
import com.fibre.optique.users.dto.UserDto;
import com.fibre.optique.users.dto.UserUpdateRequest;
import com.fibre.optique.users.entity.Role;
import com.fibre.optique.users.entity.User;
import com.fibre.optique.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // STAFF ACCOUNT CREATION — ADMIN only
    // -------------------------------------------------------------------------

    /**
     * Creates an internal staff account with any role (COMMERCIAL, TECHNICIEN, SUPPORT, etc.)
     * or a CLIENT account. Restricted to ADMIN.
     * <p>
     * Public self-registration (PROSPECT only) is handled by {@code POST /api/v1/auth/register}.
     * </p>
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(
        @Valid @RequestBody UserCreateRequest request
    ) {
        // Default to CLIENT if no role specified
        if (request.getRole() == null) {
            request.setRole(Role.CLIENT);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
            userService.createUser(request)
        );
    }

    // -------------------------------------------------------------------------
    // PROFILE
    // -------------------------------------------------------------------------

    /** Returns the profile of the currently authenticated user. */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    // -------------------------------------------------------------------------
    // COMMERCIAL — technicien list for scheduling
    // -------------------------------------------------------------------------

    /**
     * Returns the list of active TECHNICIEN accounts.
     * Accessible to COMMERCIAL and ADMIN so the schedule dialog can populate its dropdown.
     */
    @GetMapping("/techniciens")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL')")
    public ResponseEntity<Page<UserDto>> getTechniciens(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "200") int size
    ) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("nom").ascending()
        );
        return ResponseEntity.ok(
            userService.searchUsers(null, Role.TECHNICIEN, true, pageable)
        );
    }

    // -------------------------------------------------------------------------
    // ADMIN — LIST & SEARCH
    // -------------------------------------------------------------------------

    /**
     * Paginated, filterable user list — ADMIN only.
     *
     * @param query   optional search term (matches nom, prenom, email)
     * @param role    optional role filter
     * @param enabled optional enabled status filter
     * @param page    0-based page number (default 0)
     * @param size    page size (default 20)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) Role role,
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(
            userService.searchUsers(query, role, enabled, pageable)
        );
    }

    /** Get a single user by id — ADMIN only. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    /**
     * Updates a user profile.
     * <ul>
     *   <li>ADMIN → can update any user, including role and enabled status.</li>
     *   <li>Others → can only update their own profile; role/enabled are ignored.</li>
     * </ul>
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UserUpdateRequest request,
        Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        boolean isAdmin = isAdmin(authentication);

        if (!isAdmin && !currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Non-admin cannot change role or enabled status
        if (!isAdmin) {
            request.setRole(currentUser.getRole());
            request.setEnabled(currentUser.getEnabled());
        }

        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // -------------------------------------------------------------------------
    // DELETE (soft)
    // -------------------------------------------------------------------------

    /** Soft-disables a user account — ADMIN only. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------

    private boolean isAdmin(Authentication auth) {
        return (
            auth != null &&
            auth
                .getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
