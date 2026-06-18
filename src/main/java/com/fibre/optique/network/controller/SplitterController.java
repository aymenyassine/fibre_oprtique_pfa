package com.fibre.optique.network.controller;

import com.fibre.optique.network.dto.SplitterDto;
import com.fibre.optique.network.dto.SplitterRequest;
import com.fibre.optique.network.service.NetworkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network/splitters")
public class SplitterController {

    private final NetworkService networkService;

    public SplitterController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<List<SplitterDto>> getAll() {
        return ResponseEntity.ok(networkService.getAllSplitters());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN')")
    public ResponseEntity<SplitterDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(networkService.getSplitterById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SplitterDto> create(@Valid @RequestBody SplitterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(networkService.createSplitter(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SplitterDto> update(@PathVariable Long id,
                                               @Valid @RequestBody SplitterRequest request) {
        return ResponseEntity.ok(networkService.updateSplitter(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        networkService.deleteSplitter(id);
        return ResponseEntity.noContent().build();
    }
}
