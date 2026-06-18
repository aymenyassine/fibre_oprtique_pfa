package com.fibre.optique.offer.controller;

import com.fibre.optique.offer.dto.OfferDto;
import com.fibre.optique.offer.dto.OfferRequest;
import com.fibre.optique.offer.dto.OfferSummaryDto;
import com.fibre.optique.offer.dto.OfferUpdateRequest;
import com.fibre.optique.offer.entity.Technologie;
import com.fibre.optique.offer.entity.TypeEngagement;
import com.fibre.optique.offer.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    /**
     * Public catalogue — all active offers, with optional filters.
     * Accessible to any authenticated user (prospects, clients, etc.).
     */
    @GetMapping
    public ResponseEntity<List<OfferDto>> getActiveOffers(
            @RequestParam(required = false) Technologie technologie,
            @RequestParam(required = false) TypeEngagement typeEngagement) {
        return ResponseEntity.ok(offerService.getActiveOffers(technologie, typeEngagement));
    }

    /**
     * Full offer list including inactive — ADMIN only.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OfferDto>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    /**
     * Single offer detail by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OfferDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferDtoById(id));
    }

    /**
     * Lightweight summary — used internally and by front-end subscription flows.
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<OfferSummaryDto> getSummary(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferSummaryById(id));
    }

    /**
     * Add a new offer to the catalogue — ADMIN only.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferDto> createOffer(@Valid @RequestBody OfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.createOffer(request));
    }

    /**
     * Full or partial update of an offer — ADMIN only.
     * Uses a separate OfferUpdateRequest so all fields are optional.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferDto> updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody OfferUpdateRequest request) {
        return ResponseEntity.ok(offerService.updateOffer(id, request));
    }

    /**
     * Soft-deactivate an offer (hides it from the public catalogue).
     * Existing subscriptions are NOT affected.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        offerService.deactivateOffer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Hard delete — only safe when no subscriptions reference the offer.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
