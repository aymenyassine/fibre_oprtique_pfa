package com.fibre.optique.network.controller;

import com.fibre.optique.network.dto.EligibilityResponse;
import com.fibre.optique.network.dto.NetworkStatusResponse;
import com.fibre.optique.network.service.NetworkService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/network")
public class EligibilityController {

    private final NetworkService networkService;

    public EligibilityController(NetworkService networkService) {
        this.networkService = networkService;
    }

    /**
     * Checks whether GPS coordinates are eligible for fiber connection.
     * Open to any authenticated user (prospects, clients, etc.).
     *
     * @param longitude WGS84 longitude (e.g. 2.3488)
     * @param latitude  WGS84 latitude  (e.g. 48.8534)
     */
    @GetMapping("/eligibility")
    public ResponseEntity<EligibilityResponse> checkEligibility(
            @RequestParam double longitude,
            @RequestParam double latitude) {
        return ResponseEntity.ok(networkService.checkEligibility(longitude, latitude));
    }

    /**
     * Returns the full operational status of the network infrastructure.
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIEN', 'COMMERCIAL')")
    public ResponseEntity<NetworkStatusResponse> getNetworkStatus() {
        return ResponseEntity.ok(networkService.getNetworkStatus());
    }
}
