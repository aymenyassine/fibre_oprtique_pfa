package com.fibre.optique.network.controller;

import com.fibre.optique.network.dto.CheminFibreDto;
import com.fibre.optique.network.dto.CheminFibreRequest;
import com.fibre.optique.network.service.NetworkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network/chemins-fibre")
public class CheminFibreController {

    private final NetworkService networkService;

    public CheminFibreController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<List<CheminFibreDto>> getAll() {
        return ResponseEntity.ok(networkService.getAllCheminsFibre());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<CheminFibreDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(networkService.getCheminFibreById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CheminFibreDto> create(@Valid @RequestBody CheminFibreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(networkService.createCheminFibre(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<CheminFibreDto> update(@PathVariable Long id,
                                                  @Valid @RequestBody CheminFibreRequest request) {
        return ResponseEntity.ok(networkService.updateCheminFibre(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        networkService.deleteCheminFibre(id);
        return ResponseEntity.noContent().build();
    }
}
