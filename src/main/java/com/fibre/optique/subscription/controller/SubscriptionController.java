package com.fibre.optique.subscription.controller;

import com.fibre.optique.subscription.dto.AbonnementDto;
import com.fibre.optique.subscription.dto.ChangeOfferRequest;
import com.fibre.optique.subscription.dto.ContratDto;
import com.fibre.optique.subscription.dto.SubscriptionRequest;
import com.fibre.optique.subscription.service.SubscriptionService;
import com.fibre.optique.users.entity.Role;
import com.fibre.optique.users.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    /** Admin sees all subscriptions; clients see only their own. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL', 'SUPPORT')")
    public ResponseEntity<List<AbonnementDto>> getAll() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbonnementDto> getById(@PathVariable Long id,
                                                  Authentication authentication) {
        AbonnementDto dto = subscriptionService.getSubscriptionById(id);
        // Clients can only read their own subscription
        if (!isStaff(authentication)) {
            User currentUser = (User) authentication.getPrincipal();
            if (!dto.getClientId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(dto);
    }

    /** Returns all subscriptions for a given client — accessible by ADMIN/COMMERCIAL and the client themselves. */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AbonnementDto>> getByClient(@PathVariable Long clientId,
                                                            Authentication authentication) {
        if (!isStaff(authentication)) {
            User currentUser = (User) authentication.getPrincipal();
            if (!currentUser.getId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByClient(clientId));
    }

    // -------------------------------------------------------------------------
    // CREATION
    // -------------------------------------------------------------------------

    /**
     * Creates a new subscription and auto-generates the contract PDF.
     * Restricted to COMMERCIAL and ADMIN (staff creates on behalf of a client).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('COMMERCIAL', 'ADMIN')")
    public ResponseEntity<AbonnementDto> create(@Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.createSubscription(request));
    }

    // -------------------------------------------------------------------------
    // LIFECYCLE
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL', 'SUPPORT')")
    public ResponseEntity<AbonnementDto> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.suspendSubscription(id));
    }

    @PutMapping("/{id}/resilie")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL')")
    public ResponseEntity<AbonnementDto> terminate(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.terminateSubscription(id));
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL')")
    public ResponseEntity<AbonnementDto> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.reactivateSubscription(id));
    }

    @PutMapping("/{id}/change-offer")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL')")
    public ResponseEntity<AbonnementDto> changeOffer(@PathVariable Long id,
                                                      @Valid @RequestBody ChangeOfferRequest request) {
        return ResponseEntity.ok(subscriptionService.changeOffer(id, request.getNewOffreId()));
    }

    // -------------------------------------------------------------------------
    // CONTRACT
    // -------------------------------------------------------------------------

    /** Returns contract metadata (pdf key, signature date). */
    @GetMapping("/{id}/contract")
    public ResponseEntity<ContratDto> getContract(@PathVariable Long id,
                                                   Authentication authentication) {
        AbonnementDto sub = subscriptionService.getSubscriptionById(id);
        if (!isStaff(authentication)) {
            User currentUser = (User) authentication.getPrincipal();
            if (!sub.getClientId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(subscriptionService.getContrat(id));
    }

    /** Downloads the contract PDF as an octet stream. */
    @GetMapping("/{id}/contract/download")
    public ResponseEntity<byte[]> downloadContract(@PathVariable Long id,
                                                    Authentication authentication) throws IOException {
        AbonnementDto sub = subscriptionService.getSubscriptionById(id);
        if (!isStaff(authentication)) {
            User currentUser = (User) authentication.getPrincipal();
            if (!sub.getClientId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        byte[] pdfBytes = subscriptionService.downloadContrat(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("contrat-abonnement-" + id + ".pdf")
                        .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------

    private boolean isStaff(Authentication auth) {
        if (auth == null) return false;
        User user = (User) auth.getPrincipal();
        Role role = user.getRole();
        return role == Role.ADMIN || role == Role.COMMERCIAL
                || role == Role.SUPPORT || role == Role.TECHNICIEN;
    }
}
