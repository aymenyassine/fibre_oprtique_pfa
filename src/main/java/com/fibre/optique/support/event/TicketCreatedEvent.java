package com.fibre.optique.support.event;

import com.fibre.optique.support.entity.TicketPriority;
import org.springframework.context.ApplicationEvent;

/**
 * Published when a new support ticket is created.
 * Triggers a confirmation email to the client.
 */
public class TicketCreatedEvent extends ApplicationEvent {

    private final Long ticketId;
    private final String clientEmail;
    private final String clientNom;
    private final String titre;
    private final TicketPriority priorite;

    public TicketCreatedEvent(Object source, Long ticketId, String clientEmail,
                               String clientNom, String titre, TicketPriority priorite) {
        super(source);
        this.ticketId = ticketId;
        this.clientEmail = clientEmail;
        this.clientNom = clientNom;
        this.titre = titre;
        this.priorite = priorite;
    }

    public Long getTicketId()      { return ticketId; }
    public String getClientEmail()  { return clientEmail; }
    public String getClientNom()    { return clientNom; }
    public String getTitre()        { return titre; }
    public TicketPriority getPriorite() { return priorite; }
}
