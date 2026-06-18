package com.fibre.optique.network.exception;

public class NetworkResourceNotFoundException extends RuntimeException {

    public NetworkResourceNotFoundException(String message) {
        super(message);
    }

    public NetworkResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " introuvable avec l'identifiant : " + id);
    }
}
