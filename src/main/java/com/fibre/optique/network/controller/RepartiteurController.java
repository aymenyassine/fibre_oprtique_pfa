package com.fibre.optique.network.controller;

import com.fibre.optique.network.dto.RepartiteurDto;
import com.fibre.optique.network.dto.RepartiteurRequest;
import com.fibre.optique.network.service.NetworkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network/repartiteurs")
public class RepartiteurController {

    private final NetworkService networkService;

    public RepartiteurController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN', 'COMMERCIAL')")
    public ResponseEntity<List<RepartiteurDto>> getAll() {
        return ResponseEntity.ok(networkService.getAllRepartiteurs());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN', 'COMMERCIAL')")
    public ResponseEntity<RepartiteurDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(networkService.getRepartiteurById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RepartiteurDto> create(@Valid @RequestBody RepartiteurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(networkService.createRepartiteur(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RepartiteurDto> update(@PathVariable Long id,
                                                  @Valid @RequestBody RepartiteurRequest request) {
        return ResponseEntity.ok(networkService.updateRepartiteur(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        networkService.deleteRepartiteur(id);
        return ResponseEntity.noContent().build();
    }
}
