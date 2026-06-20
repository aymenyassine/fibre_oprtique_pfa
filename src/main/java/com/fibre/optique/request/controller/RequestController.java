package com.fibre.optique.request.controller;

import com.fibre.optique.request.dto.*;
import com.fibre.optique.request.entity.DemandeStatus;
import com.fibre.optique.request.service.RequestService;
import com.fibre.optique.users.entity.Role;
import com.fibre.optique.users.entity.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    /**
     * Paginated list of all raccordement requests — staff only.
     *
     * @param statut  optional status filter
     * @param keyword optional search term
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL', 'TECHNICIEN', 'SUPPORT')")
    public ResponseEntity<Page<DemandeRaccordementDto>> getAll(
        @RequestParam(required = false) DemandeStatus statut,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        // Technicien : ne voit que les demandes qui lui sont assignées
        if (currentUser.getRole() == Role.TECHNICIEN) {
            Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("datePlanification").ascending()
            );
            return ResponseEntity.ok(
                requestService.searchForTechnicien(
                    currentUser.getId(),
                    statut,
                    keyword,
                    pageable
                )
            );
        }

        // Autres rôles (ADMIN, COMMERCIAL, SUPPORT) : liste complète
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(
            requestService.search(statut, keyword, pageable)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL', 'TECHNICIEN', 'SUPPORT')")
    public ResponseEntity<DemandeRaccordementDto> getById(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(requestService.getById(id));
    }

    // -------------------------------------------------------------------------
    // STEP 1 — Prospect submits a request (open endpoint)
    // -------------------------------------------------------------------------

    /**
     * Accessible to any authenticated user (prospects included).
     * The eligibility check runs synchronously during this call.
     */
    @PostMapping
    public ResponseEntity<DemandeRaccordementDto> submit(
        @Valid @RequestBody DemandeRaccordementRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            requestService.submitRequest(request)
        );
    }

    // -------------------------------------------------------------------------
    // STEP 2 — COMMERCIAL sets the quote + pre-selects offer
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/devis")
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<DemandeRaccordementDto> setDevis(
        @PathVariable Long id,
        @Valid @RequestBody DevisRequest request
    ) {
        return ResponseEntity.ok(requestService.setDevis(id, request));
    }

    // -------------------------------------------------------------------------
    // STEP 3 — Prospect accepts the quote
    // -------------------------------------------------------------------------

    /**
     * Accepts a devis.
     * - Staff (ADMIN, COMMERCIAL) can accept any demande.
     * - A prospect/client can accept if their email matches prospectEmail,
     *   OR if they are a PROSPECT/CLIENT role (they can only have submitted
     *   their own demandes anyway).
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<DemandeRaccordementDto> accept(
        @PathVariable Long id,
        Authentication authentication
    ) {
        if (isStaff(authentication)) {
            // Staff accepts on behalf of the prospect — no ownership check needed
            return ResponseEntity.ok(requestService.acceptDevis(id));
        }

        // Non-staff: verify the authenticated user's email matches the demande's prospectEmail
        DemandeRaccordementDto demande = requestService.getById(id);
        User currentUser = (User) authentication.getPrincipal();
        if (
            !demande.getProspectEmail().equalsIgnoreCase(currentUser.getEmail())
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(requestService.acceptDevis(id));
    }

    // -------------------------------------------------------------------------
    // STEP 4 — ADMIN / COMMERCIAL schedules + assigns technician
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL')")
    public ResponseEntity<DemandeRaccordementDto> schedule(
        @PathVariable Long id,
        @Valid @RequestBody ScheduleRequest request
    ) {
        return ResponseEntity.ok(requestService.schedule(id, request));
    }

    // -------------------------------------------------------------------------
    // STEP 5 — TECHNICIEN marks raccordement as physically done
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('TECHNICIEN', 'ADMIN')")
    public ResponseEntity<DemandeRaccordementDto> complete(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(requestService.complete(id));
    }

    // -------------------------------------------------------------------------
    // REJECT — ADMIN at any point before TERMINE
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL')")
    public ResponseEntity<DemandeRaccordementDto> reject(
        @PathVariable Long id,
        @RequestBody(required = false) RejectRequest request
    ) {
        String raison = request != null ? request.getRaison() : null;
        return ResponseEntity.ok(requestService.reject(id, raison));
    }

    // -------------------------------------------------------------------------

    private boolean isStaff(Authentication auth) {
        if (auth == null) return false;
        User user = (User) auth.getPrincipal();
        return switch (user.getRole()) {
            case ADMIN, COMMERCIAL, TECHNICIEN, SUPPORT -> true;
            default -> false;
        };
    }
}
