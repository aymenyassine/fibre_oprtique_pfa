package com.fibre.optique.request.event;

import org.springframework.context.ApplicationEvent;

/**
 * Published when a prospect submits a new raccordement request.
 * Triggers a confirmation email to the prospect.
 */
public class DemandeSubmittedEvent extends ApplicationEvent {

    private final Long demandeId;
    private final String prospectEmail;
    private final String prospectNom;
    private final String adresse;

    public DemandeSubmittedEvent(Object source, Long demandeId,
                                  String prospectEmail, String prospectNom, String adresse) {
        super(source);
        this.demandeId = demandeId;
        this.prospectEmail = prospectEmail;
        this.prospectNom = prospectNom;
        this.adresse = adresse;
    }

    public Long getDemandeId()      { return demandeId; }
    public String getProspectEmail() { return prospectEmail; }
    public String getProspectNom()   { return prospectNom; }
    public String getAdresse()       { return adresse; }
}
