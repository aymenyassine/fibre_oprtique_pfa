package com.fibre.optique.notification.listener;

import com.fibre.optique.billing.event.InvoiceGeneratedEvent;
import com.fibre.optique.billing.event.InvoiceOverdueEvent;
import com.fibre.optique.notification.service.NotificationService;
import com.fibre.optique.request.event.DemandeCompletedEvent;
import com.fibre.optique.request.event.DemandeSubmittedEvent;
import com.fibre.optique.subscription.event.SubscriptionCreatedEvent;
import com.fibre.optique.subscription.event.SubscriptionStatusChangedEvent;
import com.fibre.optique.support.event.SlaBreachEvent;
import com.fibre.optique.support.event.TicketCreatedEvent;
import com.fibre.optique.support.event.TicketReplyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Central event listener for all application domain events.
 *
 * <p>
 * Each handler runs {@code @Async} (thread pool configured in AppConfig)
 * so notification delivery never blocks the calling transaction.
 * </p>
 *
 * <p>
 * In development (Maildev on port 1025), all emails are intercepted locally.
 * Switch {@code spring.mail.*} properties for production SMTP.
 * </p>
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;

    @Value("${app.notification.admin-email:admin@fibre-optique.local}")
    private String adminEmail;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // =========================================================================
    // SUBSCRIPTION EVENTS
    // =========================================================================

    @Async
    @EventListener
    public void onSubscriptionCreated(SubscriptionCreatedEvent event) {
        log.debug("Handling SubscriptionCreatedEvent for abonnement #{}", event.getAbonnementId());

        notificationService.sendEmail(
                event.getClientEmail(),
                "Votre abonnement Fibre Optique est activé !",
                """
                        Bonjour %s,

                        Votre abonnement à l'offre "%s" (n°%d) est maintenant actif.
                        Votre contrat est disponible dans votre espace client.

                        Bienvenue chez Fibre Optique Platform !
                        """.formatted(event.getClientNom(), event.getOfferNom(), event.getAbonnementId()));
    }

    @Async
    @EventListener
    public void onSubscriptionStatusChanged(SubscriptionStatusChangedEvent event) {
        log.debug("Handling SubscriptionStatusChangedEvent — abonnement #{} → {}",
                event.getAbonnementId(), event.getNewStatus());

        String subject = switch (event.getNewStatus()) {
            case SUSPENDU -> "Votre abonnement a été suspendu";
            case RESILIE -> "Votre abonnement a été résilié";
            default -> "Mise à jour de votre abonnement";
        };

        String body = switch (event.getNewStatus()) {
            case SUSPENDU ->
                """
                        Bonjour %s,

                        Votre abonnement (n°%d) a été temporairement suspendu.
                        Pour toute question, contactez notre service client.
                        """.formatted(event.getClientNom(), event.getAbonnementId());
            case RESILIE ->
                """
                        Bonjour %s,

                        Votre abonnement (n°%d) a été résilié.
                        Nous espérons vous revoir bientôt.
                        """.formatted(event.getClientNom(), event.getAbonnementId());
            default -> "Statut de votre abonnement mis à jour : " + event.getNewStatus();
        };

        notificationService.sendEmail(event.getClientEmail(), subject, body);
    }

    // =========================================================================
    // BILLING EVENTS
    // =========================================================================

    @Async
    @EventListener
    public void onInvoiceGenerated(InvoiceGeneratedEvent event) {
        log.debug("Handling InvoiceGeneratedEvent — facture {}", event.getReference());

        notificationService.sendEmail(
                event.getClientEmail(),
                "Votre facture " + event.getReference() + " est disponible",
                """
                        Bonjour %s,

                        Votre facture %s d'un montant de %.2f MAD TTC est disponible
                        dans votre espace client.

                        Merci de régler dans les 30 jours.
                        """.formatted(event.getClientNom(), event.getReference(), event.getMontantTTC()));
    }

    @Async
    @EventListener
    public void onInvoiceOverdue(InvoiceOverdueEvent event) {
        log.debug("Handling InvoiceOverdueEvent — facture {}", event.getReference());

        notificationService.sendEmail(
                event.getClientEmail(),
                "Relance : facture " + event.getReference() + " impayée",
                """
                        Bonjour %s,

                        Votre facture %s d'un montant de %.2f MAD TTC est en retard de paiement.
                        Veuillez régulariser votre situation au plus tôt pour éviter la suspension
                        de votre abonnement.

                        Service facturation Fibre Optique Platform
                        """.formatted(event.getClientNom(), event.getReference(), event.getMontantTTC()));
    }

    // =========================================================================
    // REQUEST (RACCORDEMENT) EVENTS
    // =========================================================================

    @Async
    @EventListener
    public void onDemandeSubmitted(DemandeSubmittedEvent event) {
        log.debug("Handling DemandeSubmittedEvent — demande #{}", event.getDemandeId());

        notificationService.sendEmail(
                event.getProspectEmail(),
                "Votre demande de raccordement a bien été reçue",
                """
                        Bonjour %s,

                        Nous avons bien reçu votre demande de raccordement (n°%d)
                        pour l'adresse : %s

                        Notre équipe commerciale va étudier votre éligibilité et vous
                        contactera dans les plus brefs délais.

                        Fibre Optique Platform
                        """.formatted(event.getProspectNom(), event.getDemandeId(), event.getAdresse()));
    }

    @Async
    @EventListener
    public void onDemandeCompleted(DemandeCompletedEvent event) {
        log.debug("Handling DemandeCompletedEvent — demande #{}", event.getDemandeId());

        notificationService.sendEmail(
                event.getProspectEmail(),
                "Votre raccordement est terminé — Bienvenue !",
                """
                        Bonjour %s,

                        Votre installation fibre (demande n°%d) est maintenant terminée.
                        Votre compte client a été créé. Vous pouvez vous connecter avec
                        votre adresse email et réinitialiser votre mot de passe.

                        Bienvenue chez Fibre Optique Platform !
                        """.formatted(event.getProspectNom(), event.getDemandeId()));
    }

    // =========================================================================
    // SUPPORT EVENTS
    // =========================================================================

    @Async
    @EventListener
    public void onTicketCreated(TicketCreatedEvent event) {
        log.debug("Handling TicketCreatedEvent — ticket #{}", event.getTicketId());

        notificationService.sendEmail(
                event.getClientEmail(),
                "Ticket d'assistance créé : " + event.getTitre(),
                """
                        Bonjour %s,

                        Votre ticket d'assistance (n°%d) a bien été enregistré.
                        Priorité : %s

                        Notre équipe support va traiter votre demande dans les meilleurs délais.

                        Fibre Optique Platform — Support
                        """.formatted(event.getClientNom(), event.getTicketId(), event.getPriorite()));
    }

    @Async
    @EventListener
    public void onTicketReply(TicketReplyEvent event) {
        log.debug("Handling TicketReplyEvent — ticket #{}", event.getTicketId());

        notificationService.sendEmail(
                event.getRecipientEmail(),
                "Nouvelle réponse sur votre ticket n°" + event.getTicketId(),
                """
                        Bonjour %s,

                        %s a ajouté une réponse à votre ticket (n°%d) :

                        "%s"

                        Connectez-vous à votre espace pour voir la réponse complète et continuer
                        la conversation.

                        Fibre Optique Platform — Support
                        """.formatted(
                        event.getRecipientNom(),
                        event.getAuteurNom(),
                        event.getTicketId(),
                        event.getMessagePreview()));
    }

    @Async
    @EventListener
    public void onSlaBreach(SlaBreachEvent event) {
        log.warn("Handling SlaBreachEvent — ticket #{} escalated to {}",
                event.getTicketId(), event.getNewPriority());

        // Alert the admin on-call
        notificationService.sendEmail(
                adminEmail,
                "[ALERTE SLA] Ticket #" + event.getTicketId() + " — " + event.getTitre(),
                """
                        ALERTE : Le SLA du ticket n°%d a été dépassé.

                        Titre    : %s
                        Client   : %s
                        Priorité : escaladée à %s

                        Veuillez prendre en charge ce ticket immédiatement.

                        Fibre Optique Platform — Système automatique
                        """.formatted(
                        event.getTicketId(),
                        event.getTitre(),
                        event.getClientEmail(),
                        event.getNewPriority()));
    }
}
