package com.fibre.optique.support.service;

import com.fibre.optique.support.dto.*;
import com.fibre.optique.support.entity.*;
import com.fibre.optique.support.event.SlaBreachEvent;
import com.fibre.optique.support.event.TicketCreatedEvent;
import com.fibre.optique.support.event.TicketReplyEvent;
import com.fibre.optique.support.exception.TicketNotFoundException;
import com.fibre.optique.support.exception.TicketValidationException;
import com.fibre.optique.support.repository.MessageTicketRepository;
import com.fibre.optique.support.repository.TicketRepository;
import com.fibre.optique.users.entity.Role;
import com.fibre.optique.users.entity.User;
import com.fibre.optique.users.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportService.class);

    private final TicketRepository ticketRepository;
    private final MessageTicketRepository messageRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public SupportService(TicketRepository ticketRepository,
                           MessageTicketRepository messageRepository,
                           UserService userService,
                           ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    // =========================================================================
    // QUERIES
    // =========================================================================

    public Page<TicketDto> search(Long clientId, Long agentId, TicketStatus statut,
                                   TicketPriority priorite, String keyword, Pageable pageable) {
        return ticketRepository.search(clientId, agentId, statut, priorite, keyword, pageable)
                .map(TicketDto::fromEntity);
    }

    public TicketDto getById(Long id) {
        return ticketRepository.findById(id)
                .map(TicketDto::fromEntity)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    public List<TicketDto> getMyTickets(Long clientId) {
        return ticketRepository.findByClientId(clientId)
                .stream()
                .map(TicketDto::fromEntity)
                .toList();
    }

    public List<MessageTicketDto> getMessages(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new TicketNotFoundException(ticketId);
        }
        return messageRepository.findByTicketIdOrderByDateEnvoiAsc(ticketId)
                .stream()
                .map(MessageTicketDto::fromEntity)
                .toList();
    }

    // =========================================================================
    // CREATE TICKET
    // =========================================================================

    /**
     * Creates a new support ticket.
     * <ul>
     *   <li>If the caller is a CLIENT / PROSPECT, they are the client of the ticket.</li>
     *   <li>Staff (ADMIN, SUPPORT) can open a ticket on behalf of a client via {@code request.clientId}.</li>
     * </ul>
     * The SLA deadline is computed immediately from the priority.
     */
    @Transactional
    public TicketDto createTicket(TicketRequest request, User currentUser) {
        User client = resolveClient(request, currentUser);

        Instant slaDeadline = Instant.now()
                .plusSeconds(request.getPriorite().slaHeures() * 3600L);

        Ticket ticket = Ticket.builder()
                .client(client)
                .titre(request.getTitre())
                .description(request.getDescription())
                .statut(TicketStatus.OUVERT)
                .priorite(request.getPriorite())
                .dateLimiteSla(slaDeadline)
                .build();

        ticket = ticketRepository.save(ticket);

        eventPublisher.publishEvent(new TicketCreatedEvent(
                this, ticket.getId(),
                client.getEmail(),
                client.getNom() + " " + client.getPrenom(),
                ticket.getTitre(),
                ticket.getPriorite()
        ));

        return TicketDto.fromEntity(ticket);
    }

    // =========================================================================
    // REPLY
    // =========================================================================

    /**
     * Adds a message to the ticket conversation.
     * Transitions the ticket from OUVERT → EN_COURS on first agent reply.
     */
    @Transactional
    public MessageTicketDto addReply(Long ticketId, MessageTicketRequest request, User author) {
        Ticket ticket = loadTicket(ticketId);

        if (ticket.getStatut() == TicketStatus.FERME) {
            throw new TicketValidationException(
                    "Impossible d'ajouter un message à un ticket fermé.");
        }

        // Auto-transition: first staff reply moves ticket to EN_COURS
        if (ticket.getStatut() == TicketStatus.OUVERT && isStaff(author)) {
            ticket.setStatut(TicketStatus.EN_COURS);
            // Assign the agent if none yet
            if (ticket.getAgent() == null) {
                ticket.setAgent(author);
            }
            ticketRepository.save(ticket);
        }

        MessageTicket message = MessageTicket.builder()
                .ticket(ticket)
                .auteur(author)
                .message(request.getMessage())
                .build();

        message = messageRepository.save(message);

        // Notify the other party
        User recipient = author.getId().equals(ticket.getClient().getId())
                ? (ticket.getAgent() != null ? ticket.getAgent() : null)
                : ticket.getClient();

        if (recipient != null) {
            String preview = request.getMessage().length() > 100
                    ? request.getMessage().substring(0, 100) + "…"
                    : request.getMessage();

            eventPublisher.publishEvent(new TicketReplyEvent(
                    this, ticketId,
                    recipient.getEmail(),
                    recipient.getNom(),
                    author.getNom() + " " + author.getPrenom(),
                    preview
            ));
        }

        return MessageTicketDto.fromEntity(message);
    }

    // =========================================================================
    // ASSIGN AGENT
    // =========================================================================

    @Transactional
    public TicketDto assignAgent(Long ticketId, AssignAgentRequest request) {
        Ticket ticket = loadTicket(ticketId);

        User agent = userService.findById(request.getAgentId())
                .orElseThrow(() -> new TicketValidationException(
                        "Agent introuvable avec l'identifiant : " + request.getAgentId()));

        if (agent.getRole() != Role.SUPPORT && agent.getRole() != Role.ADMIN) {
            throw new TicketValidationException(
                    "L'utilisateur désigné n'a pas le rôle SUPPORT ou ADMIN.");
        }

        ticket.setAgent(agent);
        if (ticket.getStatut() == TicketStatus.OUVERT) {
            ticket.setStatut(TicketStatus.EN_COURS);
        }

        return TicketDto.fromEntity(ticketRepository.save(ticket));
    }

    // =========================================================================
    // RESOLVE / CLOSE
    // =========================================================================

    /** Marks ticket as RESOLU — records the resolution timestamp. */
    @Transactional
    public TicketDto resolve(Long ticketId) {
        Ticket ticket = loadTicket(ticketId);

        if (ticket.getStatut() == TicketStatus.RESOLU || ticket.getStatut() == TicketStatus.FERME) {
            throw new TicketValidationException(
                    "Ce ticket est déjà " + ticket.getStatut() + ".");
        }

        ticket.setStatut(TicketStatus.RESOLU);
        ticket.setDateResolution(Instant.now());
        return TicketDto.fromEntity(ticketRepository.save(ticket));
    }

    /** Closes a resolved ticket — terminal state. */
    @Transactional
    public TicketDto close(Long ticketId) {
        Ticket ticket = loadTicket(ticketId);

        if (ticket.getStatut() != TicketStatus.RESOLU) {
            throw new TicketValidationException(
                    "Seul un ticket RESOLU peut être fermé. Statut actuel : " + ticket.getStatut());
        }

        ticket.setStatut(TicketStatus.FERME);
        return TicketDto.fromEntity(ticketRepository.save(ticket));
    }

    // =========================================================================
    // SLA CHECK — called by SupportSlaScheduler every hour
    // =========================================================================

    /**
     * Scans all open/in-progress tickets whose SLA deadline is past.
     * Escalates their priority to CRITIQUE and resets a new 4-hour SLA window.
     * Publishes {@link SlaBreachEvent} for each breached ticket.
     */
    @Transactional
    public void checkSlaBreaches() {
        List<Ticket> breached = ticketRepository.findSlaBreached(Instant.now(),
                List.of(TicketStatus.RESOLU, TicketStatus.FERME));
        log.info("SLA check: {} ticket(s) in breach", breached.size());

        for (Ticket ticket : breached) {
            TicketPriority oldPriority = ticket.getPriorite();

            // Escalate to CRITIQUE and extend SLA by 4 hours from now
            ticket.setPriorite(TicketPriority.CRITIQUE);
            ticket.setDateLimiteSla(Instant.now()
                    .plusSeconds(TicketPriority.CRITIQUE.slaHeures() * 3600L));

            ticketRepository.save(ticket);

            log.warn("SLA breached for ticket #{} — escalated from {} to CRITIQUE",
                    ticket.getId(), oldPriority);

            eventPublisher.publishEvent(new SlaBreachEvent(
                    this,
                    ticket.getId(),
                    ticket.getClient().getEmail(),
                    ticket.getTitre(),
                    TicketPriority.CRITIQUE
            ));
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Ticket loadTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    /**
     * Determines the client for the ticket.
     * Staff can specify a clientId; end-users are always the client themselves.
     */
    private User resolveClient(TicketRequest request, User currentUser) {
        if (isStaff(currentUser) && request.getClientId() != null) {
            return userService.findById(request.getClientId())
                    .orElseThrow(() -> new TicketValidationException(
                            "Client introuvable avec l'identifiant : " + request.getClientId()));
        }
        return currentUser;
    }

    private boolean isStaff(User user) {
        return switch (user.getRole()) {
            case ADMIN, SUPPORT, TECHNICIEN, COMMERCIAL -> true;
            default -> false;
        };
    }
}
