package com.fibre.optique.network.controller;

import com.fibre.optique.network.dto.EquipementDto;
import com.fibre.optique.network.dto.EquipementRequest;
import com.fibre.optique.network.service.NetworkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network/equipements")
public class EquipementController {

    private final NetworkService networkService;

    public EquipementController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<List<EquipementDto>> getAll() {
        return ResponseEntity.ok(networkService.getAllEquipements());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<EquipementDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(networkService.getEquipementById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipementDto> create(@Valid @RequestBody EquipementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(networkService.createEquipement(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<EquipementDto> update(@PathVariable Long id,
                                                 @Valid @RequestBody EquipementRequest request) {
        return ResponseEntity.ok(networkService.updateEquipement(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        networkService.deleteEquipement(id);
        return ResponseEntity.noContent().build();
    }
}
