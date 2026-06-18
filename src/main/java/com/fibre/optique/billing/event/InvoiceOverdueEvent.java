package com.fibre.optique.billing.event;

import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Published when an invoice transitions from EN_ATTENTE → EN_RETARD.
 * Triggers a payment reminder email to the client.
 */
public class InvoiceOverdueEvent extends ApplicationEvent {

    private final Long factureId;
    private final String clientEmail;
    private final String clientNom;
    private final String reference;
    private final BigDecimal montantTTC;

    public InvoiceOverdueEvent(Object source, Long factureId, String clientEmail,
                                String clientNom, String reference, BigDecimal montantTTC) {
        super(source);
        this.factureId = factureId;
        this.clientEmail = clientEmail;
        this.clientNom = clientNom;
        this.reference = reference;
        this.montantTTC = montantTTC;
    }

    public Long getFactureId()    { return factureId; }
    public String getClientEmail() { return clientEmail; }
    public String getClientNom()   { return clientNom; }
    public String getReference()   { return reference; }
    public BigDecimal getMontantTTC() { return montantTTC; }
}
