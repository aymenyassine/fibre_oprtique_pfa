package com.fibre.optique.request.exception;

public class DemandeNotFoundException extends RuntimeException {

    public DemandeNotFoundException(Long id) {
        super("Demande de raccordement introuvable avec l'identifiant : " + id);
    }
}
