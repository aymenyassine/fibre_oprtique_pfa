package com.fibre.optique.subscription.event;

import com.fibre.optique.subscription.entity.AbonnementStatus;
import org.springframework.context.ApplicationEvent;

/**
 * Published when a subscription status changes (suspended or terminated).
 */
public class SubscriptionStatusChangedEvent extends ApplicationEvent {

    private final Long abonnementId;
    private final String clientEmail;
    private final String clientNom;
    private final AbonnementStatus newStatus;

    public SubscriptionStatusChangedEvent(Object source, Long abonnementId,
                                          String clientEmail, String clientNom,
                                          AbonnementStatus newStatus) {
        super(source);
        this.abonnementId = abonnementId;
        this.clientEmail = clientEmail;
        this.clientNom = clientNom;
        this.newStatus = newStatus;
    }

    public Long getAbonnementId() { return abonnementId; }
    public String getClientEmail() { return clientEmail; }
    public String getClientNom() { return clientNom; }
    public AbonnementStatus getNewStatus() { return newStatus; }
}
