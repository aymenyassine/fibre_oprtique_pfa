package com.fibre.optique.network.controller;

import com.fibre.optique.network.dto.DatacenterDto;
import com.fibre.optique.network.dto.DatacenterRequest;
import com.fibre.optique.network.service.NetworkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network/datacenters")
public class DatacenterController {

    private final NetworkService networkService;

    public DatacenterController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN', 'COMMERCIAL')")
    public ResponseEntity<List<DatacenterDto>> getAll() {
        return ResponseEntity.ok(networkService.getAllDatacenters());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN', 'COMMERCIAL')")
    public ResponseEntity<DatacenterDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(networkService.getDatacenterById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DatacenterDto> create(@Valid @RequestBody DatacenterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(networkService.createDatacenter(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DatacenterDto> update(@PathVariable Long id,
                                                 @Valid @RequestBody DatacenterRequest request) {
        return ResponseEntity.ok(networkService.updateDatacenter(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        networkService.deleteDatacenter(id);
        return ResponseEntity.noContent().build();
    }
}
