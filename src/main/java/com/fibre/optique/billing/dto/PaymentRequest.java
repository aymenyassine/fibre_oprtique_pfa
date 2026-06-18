package com.fibre.optique.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Used to record or simulate a payment for an invoice.
 * The amount must match the invoice TTC — validated in the service layer.
 */
@Data
public class PaymentRequest {

    @NotNull(message = "Le montant du paiement est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    private BigDecimal montant;

    /** Optional payment reference (bank transfer id, card auth code, etc.) */
    private String referenceExterne;
}
