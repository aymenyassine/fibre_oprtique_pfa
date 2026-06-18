package com.fibre.optique.subscription.exception;

public class ContratNotFoundException extends RuntimeException {

    public ContratNotFoundException(Long abonnementId) {
        super("Contrat introuvable pour l'abonnement : " + abonnementId);
    }
}
