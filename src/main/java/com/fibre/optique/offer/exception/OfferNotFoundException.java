package com.fibre.optique.offer.exception;

public class OfferNotFoundException extends RuntimeException {

    public OfferNotFoundException(Long id) {
        super("Offre introuvable avec l'identifiant : " + id);
    }

    public OfferNotFoundException(String message) {
        super(message);
    }
}
