package com.fibre.optique.network.controller;

import com.fibre.optique.network.dto.BoiteClientDto;
import com.fibre.optique.network.dto.BoiteClientRequest;
import com.fibre.optique.network.service.NetworkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network/boites-client")
public class BoiteClientController {

    private final NetworkService networkService;

    public BoiteClientController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<List<BoiteClientDto>> getAll() {
        return ResponseEntity.ok(networkService.getAllBoitesClient());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<BoiteClientDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(networkService.getBoiteClientById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BoiteClientDto> create(@Valid @RequestBody BoiteClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(networkService.createBoiteClient(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BoiteClientDto> update(@PathVariable Long id,
                                                  @Valid @RequestBody BoiteClientRequest request) {
        return ResponseEntity.ok(networkService.updateBoiteClient(id, request));
    }

    /**
     * Incrémente le compteur de ports utilisés lors d'un raccordement client.
     * Utilisé par TECHNICIEN après installation physique.
     */
    @PatchMapping("/{id}/connect")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<BoiteClientDto> connectPort(@PathVariable Long id) {
        return ResponseEntity.ok(networkService.incrementPortsUtilises(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        networkService.deleteBoiteClient(id);
        return ResponseEntity.noContent().build();
    }
}
