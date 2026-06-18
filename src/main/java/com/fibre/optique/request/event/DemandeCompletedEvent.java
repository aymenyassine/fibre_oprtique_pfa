package com.fibre.optique.request.event;

import org.springframework.context.ApplicationEvent;

/**
 * Published when a technician marks a raccordement request as TERMINE.
 * Triggers auto account creation + subscription.
 */
public class DemandeCompletedEvent extends ApplicationEvent {

    private final Long demandeId;
    private final String prospectEmail;
    private final String prospectNom;

    public DemandeCompletedEvent(Object source, Long demandeId,
                                  String prospectEmail, String prospectNom) {
        super(source);
        this.demandeId = demandeId;
        this.prospectEmail = prospectEmail;
        this.prospectNom = prospectNom;
    }

    public Long getDemandeId()      { return demandeId; }
    public String getProspectEmail() { return prospectEmail; }
    public String getProspectNom()   { return prospectNom; }
}
