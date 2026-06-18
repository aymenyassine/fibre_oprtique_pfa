package com.fibre.optique.subscription.exception;

public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(Long id) {
        super("Abonnement introuvable avec l'identifiant : " + id);
    }
}
