package com.fibre.optique.support.event;

import org.springframework.context.ApplicationEvent;

/**
 * Published when a new reply is added to a ticket.
 * Notifies the other party (client or agent) about the new message.
 */
public class TicketReplyEvent extends ApplicationEvent {

    private final Long ticketId;
    private final String recipientEmail;
    private final String recipientNom;
    private final String auteurNom;
    private final String messagePreview;

    public TicketReplyEvent(Object source, Long ticketId, String recipientEmail,
                             String recipientNom, String auteurNom, String messagePreview) {
        super(source);
        this.ticketId = ticketId;
        this.recipientEmail = recipientEmail;
        this.recipientNom = recipientNom;
        this.auteurNom = auteurNom;
        this.messagePreview = messagePreview;
    }

    public Long getTicketId()        { return ticketId; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getRecipientNom()   { return recipientNom; }
    public String getAuteurNom()      { return auteurNom; }
    public String getMessagePreview() { return messagePreview; }
}
