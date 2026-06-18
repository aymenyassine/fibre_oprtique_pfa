package com.fibre.optique.support.controller;

import com.fibre.optique.support.dto.*;
import com.fibre.optique.support.entity.TicketPriority;
import com.fibre.optique.support.entity.TicketStatus;
import com.fibre.optique.support.service.SupportService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    /**
     * Paginated, filterable ticket list — staff only.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'TECHNICIEN', 'COMMERCIAL')")
    public ResponseEntity<Page<TicketDto>> getAll(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) TicketStatus statut,
            @RequestParam(required = false) TicketPriority priorite,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return ResponseEntity.ok(
                supportService.search(clientId, agentId, statut, priorite, keyword, pageable));
    }

    /** Returns the authenticated client's own tickets. */
    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketDto>> getMyTickets(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(supportService.getMyTickets(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getById(@PathVariable Long id,
                                              Authentication authentication) {
        TicketDto dto = supportService.getById(id);
        // Clients can only view their own tickets
        if (!isStaff(authentication)) {
            User currentUser = (User) authentication.getPrincipal();
            if (!dto.getClientId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(dto);
    }

    // -------------------------------------------------------------------------
    // MESSAGES (conversation thread)
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageTicketDto>> getMessages(
            @PathVariable Long id, Authentication authentication) {

        TicketDto dto = supportService.getById(id);
        if (!isStaff(authentication)) {
            User currentUser = (User) authentication.getPrincipal();
            if (!dto.getClientId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(supportService.getMessages(id));
    }

    // -------------------------------------------------------------------------
    // CREATE TICKET
    // -------------------------------------------------------------------------

    /**
     * Any authenticated user can open a ticket.
     * Staff can pass clientId to create on behalf of a client.
     */
    @PostMapping
    public ResponseEntity<TicketDto> create(@Valid @RequestBody TicketRequest request,
                                             Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supportService.createTicket(request, currentUser));
    }

    // -------------------------------------------------------------------------
    // REPLY
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/replies")
    public ResponseEntity<MessageTicketDto> addReply(
            @PathVariable Long id,
            @Valid @RequestBody MessageTicketRequest request,
            Authentication authentication) {

        TicketDto dto = supportService.getById(id);
        User currentUser = (User) authentication.getPrincipal();

        // Only the ticket's client or staff can reply
        if (!isStaff(authentication) && !dto.getClientId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supportService.addReply(id, request, currentUser));
    }

    // -------------------------------------------------------------------------
    // ASSIGN AGENT
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<TicketDto> assignAgent(
            @PathVariable Long id,
            @Valid @RequestBody AssignAgentRequest request) {
        return ResponseEntity.ok(supportService.assignAgent(id, request));
    }

    // -------------------------------------------------------------------------
    // RESOLVE / CLOSE
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'TECHNICIEN')")
    public ResponseEntity<TicketDto> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(supportService.resolve(id));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<TicketDto> close(@PathVariable Long id) {
        return ResponseEntity.ok(supportService.close(id));
    }

    // -------------------------------------------------------------------------
    // MANUAL SLA CHECK (admin convenience / testing)
    // -------------------------------------------------------------------------

    @PostMapping("/admin/check-sla")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> triggerSlaCheck() {
        supportService.checkSlaBreaches();
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------

    private boolean isStaff(Authentication auth) {
        if (auth == null) return false;
        User user = (User) auth.getPrincipal();
        return switch (user.getRole()) {
            case ADMIN, SUPPORT, TECHNICIEN, COMMERCIAL -> true;
            default -> false;
        };
    }
}
