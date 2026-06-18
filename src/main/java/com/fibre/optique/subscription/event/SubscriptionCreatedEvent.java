package com.fibre.optique.subscription.event;

import org.springframework.context.ApplicationEvent;

/**
 * Published after a new subscription is successfully created.
 * Consumed by the notification module to send a confirmation email.
 */
public class SubscriptionCreatedEvent extends ApplicationEvent {

    private final Long abonnementId;
    private final String clientEmail;
    private final String clientNom;
    private final String offerNom;

    public SubscriptionCreatedEvent(Object source, Long abonnementId,
                                    String clientEmail, String clientNom, String offerNom) {
        super(source);
        this.abonnementId = abonnementId;
        this.clientEmail = clientEmail;
        this.clientNom = clientNom;
        this.offerNom = offerNom;
    }

    public Long getAbonnementId() { return abonnementId; }
    public String getClientEmail() { return clientEmail; }
    public String getClientNom() { return clientNom; }
    public String getOfferNom() { return offerNom; }
}
