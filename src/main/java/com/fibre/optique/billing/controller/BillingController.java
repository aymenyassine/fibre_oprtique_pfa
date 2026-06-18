package com.fibre.optique.billing.controller;

import com.fibre.optique.billing.dto.BillingStatsDto;
import com.fibre.optique.billing.dto.FactureDto;
import com.fibre.optique.billing.dto.PaymentRequest;
import com.fibre.optique.billing.entity.FactureStatus;
import com.fibre.optique.billing.service.BillingService;
import com.fibre.optique.users.entity.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    // -------------------------------------------------------------------------
    // CLIENT — own invoices
    // -------------------------------------------------------------------------

    /**
     * Returns all invoices for the authenticated client.
     * Staff can use GET /invoices with a clientId filter instead.
     */
    @GetMapping("/my-invoices")
    public ResponseEntity<List<FactureDto>> getMyInvoices(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(billingService.getMyInvoices(currentUser.getId()));
    }

    // -------------------------------------------------------------------------
    // ADMIN / STAFF — full invoice list
    // -------------------------------------------------------------------------

    /**
     * Paginated, filterable invoice list — staff only.
     */
    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL', 'SUPPORT')")
    public ResponseEntity<Page<FactureDto>> getAllInvoices(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) FactureStatus statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dateEmission").descending());
        return ResponseEntity.ok(billingService.search(clientId, statut, from, to, pageable));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<FactureDto> getById(@PathVariable Long id,
                                               Authentication authentication) {
        FactureDto dto = billingService.getById(id);

        // Clients can only view their own invoices
        if (!isStaff(authentication)) {
            User currentUser = (User) authentication.getPrincipal();
            if (!dto.getClientId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(dto);
    }

    // -------------------------------------------------------------------------
    // PDF DOWNLOAD
    // -------------------------------------------------------------------------

    @GetMapping("/invoices/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id,
                                               Authentication authentication) throws IOException {
        FactureDto dto = billingService.getById(id);

        if (!isStaff(authentication)) {
            User currentUser = (User) authentication.getPrincipal();
            if (!dto.getClientId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        byte[] pdfBytes = billingService.downloadInvoicePdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(dto.getReference() + ".pdf")
                        .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // PAYMENT
    // -------------------------------------------------------------------------

    /**
     * Records payment for an invoice.
     * Clients can pay their own; staff can pay any.
     */
    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<FactureDto> pay(@PathVariable Long id,
                                           @Valid @RequestBody PaymentRequest request,
                                           Authentication authentication) {
        FactureDto dto = billingService.getById(id);

        if (!isStaff(authentication)) {
            User currentUser = (User) authentication.getPrincipal();
            if (!dto.getClientId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(billingService.recordPayment(id, request));
    }

    // -------------------------------------------------------------------------
    // CANCEL
    // -------------------------------------------------------------------------

    @PatchMapping("/invoices/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL')")
    public ResponseEntity<FactureDto> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.cancelInvoice(id));
    }

    // -------------------------------------------------------------------------
    // STATS
    // -------------------------------------------------------------------------

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMERCIAL')")
    public ResponseEntity<BillingStatsDto> getStats() {
        return ResponseEntity.ok(billingService.getStats());
    }

    // -------------------------------------------------------------------------
    // MANUAL TRIGGER (dev / admin convenience)
    // -------------------------------------------------------------------------

    /**
     * Manually triggers monthly invoice generation.
     * Useful for testing without waiting for the scheduler.
     */
    @PostMapping("/admin/generate-invoices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> triggerMonthlyGeneration() {
        billingService.generateMonthlyInvoices();
        return ResponseEntity.ok().build();
    }

    /**
     * Manually triggers the overdue check.
     */
    @PostMapping("/admin/check-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> triggerOverdueCheck() {
        billingService.markOverdueInvoices();
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------

    private boolean isStaff(Authentication auth) {
        if (auth == null) return false;
        User user = (User) auth.getPrincipal();
        return switch (user.getRole()) {
            case ADMIN, COMMERCIAL, SUPPORT, TECHNICIEN -> true;
            default -> false;
        };
    }
}
