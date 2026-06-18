package com.fibre.optique.billing.event;

import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Published after a new invoice is created (monthly scheduler or manual trigger).
 * Consumed by the notification module to email the client.
 */
public class InvoiceGeneratedEvent extends ApplicationEvent {

    private final Long factureId;
    private final String clientEmail;
    private final String clientNom;
    private final String reference;
    private final BigDecimal montantTTC;

    public InvoiceGeneratedEvent(Object source, Long factureId, String clientEmail,
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
