package com.fibre.optique.request.dto;

import lombok.Data;

/**
 * Optional rejection reason when an ADMIN rejects a request.
 */
@Data
public class RejectRequest {

    private String raison;
}
