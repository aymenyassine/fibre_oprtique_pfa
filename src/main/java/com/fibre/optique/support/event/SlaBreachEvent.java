package com.fibre.optique.support.event;

import com.fibre.optique.support.entity.TicketPriority;
import org.springframework.context.ApplicationEvent;

/**
 * Published when a ticket's SLA deadline is breached.
 * Triggers an escalation alert email to the on-call admin.
 */
public class SlaBreachEvent extends ApplicationEvent {

    private final Long ticketId;
    private final String clientEmail;
    private final String titre;
    private final TicketPriority newPriority;

    public SlaBreachEvent(Object source, Long ticketId, String clientEmail,
                           String titre, TicketPriority newPriority) {
        super(source);
        this.ticketId = ticketId;
        this.clientEmail = clientEmail;
        this.titre = titre;
        this.newPriority = newPriority;
    }

    public Long getTicketId()          { return ticketId; }
    public String getClientEmail()      { return clientEmail; }
    public String getTitre()            { return titre; }
    public TicketPriority getNewPriority() { return newPriority; }
}
