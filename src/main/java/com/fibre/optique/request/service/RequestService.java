package com.fibre.optique.request.service;

import com.fibre.optique.network.dto.EligibilityResponse;
import com.fibre.optique.network.service.NetworkService;
import com.fibre.optique.request.dto.*;
import com.fibre.optique.request.entity.DemandeRaccordement;
import com.fibre.optique.request.entity.DemandeStatus;
import com.fibre.optique.request.event.DemandeCompletedEvent;
import com.fibre.optique.request.event.DemandeSubmittedEvent;
import com.fibre.optique.request.exception.DemandeNotFoundException;
import com.fibre.optique.request.exception.DemandeValidationException;
import com.fibre.optique.request.repository.DemandeRaccordementRepository;
import com.fibre.optique.subscription.dto.SubscriptionRequest;
import com.fibre.optique.subscription.service.SubscriptionService;
import com.fibre.optique.users.dto.UserCreateRequest;
import com.fibre.optique.users.dto.UserDto;
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

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestService.class);

    private final DemandeRaccordementRepository demandeRepository;
    private final NetworkService networkService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final ApplicationEventPublisher eventPublisher;

    public RequestService(DemandeRaccordementRepository demandeRepository,
                          NetworkService networkService,
                          UserService userService,
                          SubscriptionService subscriptionService,
                          ApplicationEventPublisher eventPublisher) {
        this.demandeRepository = demandeRepository;
        this.networkService = networkService;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.eventPublisher = eventPublisher;
    }

    // =========================================================================
    // QUERIES
    // =========================================================================

    public Page<DemandeRaccordementDto> search(DemandeStatus statut, String keyword, Pageable pageable) {
        return demandeRepository.search(statut, keyword, pageable)
                .map(DemandeRaccordementDto::fromEntity);
    }

    public DemandeRaccordementDto getById(Long id) {
        return demandeRepository.findById(id)
                .map(DemandeRaccordementDto::fromEntity)
                .orElseThrow(() -> new DemandeNotFoundException(id));
    }

    // =========================================================================
    // STEP 1 — Prospect submits a request
    // =========================================================================

    /**
     * Creates a new raccordement request and immediately runs an eligibility check
     * on the provided address against the network infrastructure.
     *
     * @param request prospect contact details + address
     * @return the saved demande with eligibility status populated
     */
    @Transactional
    public DemandeRaccordementDto submitRequest(DemandeRaccordementRequest request) {

        EligibilityResponse eligibility;
        DemandeStatus initialStatus;

        if (request.getLatitude() != null && request.getLongitude() != null) {
            // GPS coordinates provided → real geospatial check
            eligibility = networkService.checkEligibility(
                    request.getLongitude(), request.getLatitude());
            initialStatus = eligibility.isEligible()
                    ? DemandeStatus.ELIGIBILITE_VERIFIEE
                    : DemandeStatus.REJETE;
        } else {
            // No coordinates → save as SOUMISE for manual review by commercial
            eligibility = EligibilityResponse.builder()
                    .eligible(false)
                    .message("Coordonnées GPS non fournies — vérification manuelle requise.")
                    .build();
            initialStatus = DemandeStatus.SOUMISE;
        }

        DemandeRaccordement demande = DemandeRaccordement.builder()
                .prospectNom(request.getProspectNom())
                .prospectPrenom(request.getProspectPrenom())
                .prospectEmail(request.getProspectEmail())
                .prospectTelephone(request.getProspectTelephone())
                .adresseRaccordement(request.getAdresseRaccordement())
                .statut(initialStatus)
                .technologieDisponible(eligibility.getTechnologieDisponible())
                .build();

        demande = demandeRepository.save(demande);

        eventPublisher.publishEvent(new DemandeSubmittedEvent(
                this,
                demande.getId(),
                demande.getProspectEmail(),
                demande.getProspectNom() + " " + demande.getProspectPrenom(),
                demande.getAdresseRaccordement()
        ));

        return DemandeRaccordementDto.fromEntity(demande);
    }

    // =========================================================================
    // STEP 2 — COMMERCIAL sets the installation quote + pre-selects offer
    // =========================================================================

    @Transactional
    public DemandeRaccordementDto setDevis(Long id, DevisRequest request) {
        DemandeRaccordement demande = load(id);

        // Accept both SOUMISE (manual review) and ELIGIBILITE_VERIFIEE (auto-checked)
        if (demande.getStatut() != DemandeStatus.ELIGIBILITE_VERIFIEE
                && demande.getStatut() != DemandeStatus.SOUMISE) {
            throw new DemandeValidationException(
                    "Le devis ne peut être généré que pour une demande SOUMISE ou avec éligibilité vérifiée."
                    + " Statut actuel : " + demande.getStatut());
        }

        demande.setMontantDevis(request.getMontantDevis());
        demande.setOffreIdChoisie(request.getOffreId());
        demande.setStatut(DemandeStatus.DEVIS_GENERE);

        return DemandeRaccordementDto.fromEntity(demandeRepository.save(demande));
    }

    // =========================================================================
    // STEP 3 — Prospect accepts the quote
    // =========================================================================

    @Transactional
    public DemandeRaccordementDto acceptDevis(Long id) {
        DemandeRaccordement demande = load(id);

        requireStatus(demande, DemandeStatus.DEVIS_GENERE,
                "Seule une demande avec un devis généré peut être acceptée.");

        demande.setStatut(DemandeStatus.ACCEPTE);
        return DemandeRaccordementDto.fromEntity(demandeRepository.save(demande));
    }

    // =========================================================================
    // STEP 4 — ADMIN / COMMERCIAL schedules the installation
    // =========================================================================

    @Transactional
    public DemandeRaccordementDto schedule(Long id, ScheduleRequest request) {
        DemandeRaccordement demande = load(id);

        requireStatus(demande, DemandeStatus.ACCEPTE,
                "Une demande doit être acceptée avant d'être planifiée.");

        User technicien = userService.findById(request.getTechnicienId())
                .orElseThrow(() -> new DemandeValidationException(
                        "Technicien introuvable avec l'identifiant : " + request.getTechnicienId()));

        if (technicien.getRole() != Role.TECHNICIEN) {
            throw new DemandeValidationException(
                    "L'utilisateur désigné n'a pas le rôle TECHNICIEN : " + technicien.getEmail());
        }

        demande.setTechnicien(technicien);
        demande.setDatePlanification(request.getDatePlanification());
        demande.setStatut(DemandeStatus.PLANIFIE);

        return DemandeRaccordementDto.fromEntity(demandeRepository.save(demande));
    }

    // =========================================================================
    // STEP 5 — TECHNICIEN marks the raccordement as physically complete
    //          → auto-creates user account + subscription
    // =========================================================================

    /**
     * Marks a raccordement as complete.
     * <ol>
     *   <li>Creates a CLIENT user account for the prospect (random temp password).</li>
     *   <li>Creates a subscription for the selected offer.</li>
     *   <li>Stores the created user id back on the demande.</li>
     *   <li>Publishes {@link DemandeCompletedEvent} for email notification.</li>
     * </ol>
     */
    @Transactional
    public DemandeRaccordementDto complete(Long id) {
        DemandeRaccordement demande = load(id);

        requireStatus(demande, DemandeStatus.PLANIFIE,
                "Seule une demande planifiée peut être complétée.");

        if (demande.getOffreIdChoisie() == null) {
            throw new DemandeValidationException(
                    "Aucune offre n'a été sélectionnée pour cette demande. Veuillez d'abord générer un devis.");
        }

        // 1. Create CLIENT user account
        UserDto newClient = provisionClientAccount(demande);

        // 2. Create subscription
        SubscriptionRequest subRequest = new SubscriptionRequest();
        subRequest.setClientId(newClient.getId());
        subRequest.setOffreId(demande.getOffreIdChoisie());

        try {
            subscriptionService.createSubscription(subRequest);
        } catch (Exception e) {
            // Log and continue — subscription failure should not block completion
            log.error("Auto-subscription failed for demande {}: {}", id, e.getMessage(), e);
        }

        // 3. Update demande
        demande.setStatut(DemandeStatus.TERMINE);
        demande.setClientCreatedId(newClient.getId());
        demande = demandeRepository.save(demande);

        // 4. Notify (async)
        eventPublisher.publishEvent(new DemandeCompletedEvent(
                this,
                demande.getId(),
                demande.getProspectEmail(),
                demande.getProspectNom() + " " + demande.getProspectPrenom()
        ));

        return DemandeRaccordementDto.fromEntity(demande);
    }

    // =========================================================================
    // REJECT — ADMIN can reject at any point before TERMINE
    // =========================================================================

    @Transactional
    public DemandeRaccordementDto reject(Long id, String raison) {
        DemandeRaccordement demande = load(id);

        if (demande.getStatut() == DemandeStatus.TERMINE) {
            throw new DemandeValidationException("Une demande terminée ne peut pas être rejetée.");
        }

        demande.setStatut(DemandeStatus.REJETE);
        return DemandeRaccordementDto.fromEntity(demandeRepository.save(demande));
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private DemandeRaccordement load(Long id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new DemandeNotFoundException(id));
    }

    private void requireStatus(DemandeRaccordement demande,
                                DemandeStatus required, String message) {
        if (demande.getStatut() != required) {
            throw new DemandeValidationException(
                    message + " Statut actuel : " + demande.getStatut());
        }
    }

    /**
     * Creates a CLIENT user account from the prospect's contact details.
     * A random temporary password is generated — the client should reset it via email.
     */
    private UserDto provisionClientAccount(DemandeRaccordement demande) {
        // If an account already exists for this email, reuse it
        return userService.findByEmail(demande.getProspectEmail())
                .map(existingUser -> {
                    log.info("User account already exists for {}, reusing id={}",
                            demande.getProspectEmail(), existingUser.getId());
                    return com.fibre.optique.users.dto.UserDto.fromEntity(existingUser);
                })
                .orElseGet(() -> {
                    UserCreateRequest userRequest = new UserCreateRequest();
                    userRequest.setNom(demande.getProspectNom());
                    userRequest.setPrenom(demande.getProspectPrenom());
                    userRequest.setEmail(demande.getProspectEmail());
                    // Temp password — client must reset via "forgot password" flow
                    userRequest.setPassword(UUID.randomUUID().toString());
                    userRequest.setRole(Role.CLIENT);
                    return userService.createUser(userRequest);
                });
    }
}
