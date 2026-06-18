package com.fibre.optique.users.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Utilisateur introuvable avec l'identifiant : " + id);
    }

    public UserNotFoundException(String email) {
        super("Utilisateur introuvable avec l'email : " + email);
    }
}
