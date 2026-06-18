package com.fibre.optique.billing.exception;

public class FactureNotFoundException extends RuntimeException {

    public FactureNotFoundException(Long id) {
        super("Facture introuvable avec l'identifiant : " + id);
    }

    public FactureNotFoundException(String reference) {
        super("Facture introuvable avec la référence : " + reference);
    }
}
