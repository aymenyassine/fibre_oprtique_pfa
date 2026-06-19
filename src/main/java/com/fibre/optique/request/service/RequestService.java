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
                    ? DemandeStatus.EN_ANALYSE
                    : DemandeStatus.REJETEE;
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

        // Accept both SOUMISE (manual review) and EN_ANALYSE (auto-checked)
        if (demande.getStatut() != DemandeStatus.EN_ANALYSE
                && demande.getStatut() != DemandeStatus.SOUMISE) {
            throw new DemandeValidationException(
                    "Le devis ne peut être généré que pour une demande SOUMISE ou en analyse."
                    + " Statut actuel : " + demande.getStatut());
        }

        demande.setMontantDevis(request.getMontantDevis());
        demande.setOffreIdChoisie(request.getOffreId());
        demande.setStatut(DemandeStatus.DEVIS_ENVOYE);

        return DemandeRaccordementDto.fromEntity(demandeRepository.save(demande));
    }

    // =========================================================================
    // STEP 3 — Prospect accepts the quote
    // =========================================================================

    @Transactional
    public DemandeRaccordementDto acceptDevis(Long id) {
        DemandeRaccordement demande = load(id);

        requireStatus(demande, DemandeStatus.DEVIS_ENVOYE,
                "Seule une demande avec un devis envoyé peut être acceptée.");

        demande.setStatut(DemandeStatus.ACCEPTEE);
        return DemandeRaccordementDto.fromEntity(demandeRepository.save(demande));
    }

    // =========================================================================
    // STEP 4 — ADMIN / COMMERCIAL schedules the installation
    // =========================================================================

    @Transactional
    public DemandeRaccordementDto schedule(Long id, ScheduleRequest request) {
        DemandeRaccordement demande = load(id);

        requireStatus(demande, DemandeStatus.ACCEPTEE,
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
        demande.setStatut(DemandeStatus.PLANIFIEE);

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
    // RequestService.java

@Transactional
public DemandeRaccordementDto complete(Long id) {
    DemandeRaccordement demande = load(id);
    
    requireStatus(demande, DemandeStatus.PLANIFIEE,
            "Seule une demande planifiée peut être complétée.");
    
    if (demande.getOffreIdChoisie() == null) {
        throw new DemandeValidationException(
                "Aucune offre n'a été sélectionnée pour cette demande.");
    }
    
    // 1. Récupérer ou créer l'utilisateur (en PROSPECT)
    User user = getOrCreateProspect(demande);
    
    // 2. Créer l'abonnement
    SubscriptionRequest subRequest = new SubscriptionRequest();
    subRequest.setClientId(user.getId());
    subRequest.setOffreId(demande.getOffreIdChoisie());
    subscriptionService.createSubscription(subRequest);
    
    // 3. ✅ CHANGER LE RÔLE DIRECTEMENT
    if (user.getRole() == Role.PROSPECT) {
        user.setRole(Role.CLIENT);
        userService.save(user);
        log.info("🔄 Rôle changé: PROSPECT → CLIENT pour {} (ID: {})", 
            user.getEmail(), user.getId());
    }
    
    // 4. Mettre à jour la demande
    demande.setStatut(DemandeStatus.TERMINE);
    demande.setClientCreatedId(user.getId());
    demande = demandeRepository.save(demande);
    
    // 5. Notifications
    eventPublisher.publishEvent(new DemandeCompletedEvent(
            this,
            demande.getId(),
            demande.getProspectEmail(),
            demande.getProspectNom() + " " + demande.getProspectPrenom()
    ));
    
    return DemandeRaccordementDto.fromEntity(demande);
}

/**
 * ✅ Récupère ou crée l'utilisateur en tant que PROSPECT
 */
@Transactional
private User getOrCreateProspect(DemandeRaccordement demande) {
    // Vérifier si l'utilisateur existe déjà
    return userService.findByEmail(demande.getProspectEmail())
        .map(existingUser -> {
            // Si déjà CLIENT → on garde (cas d'un 2ème raccordement)
            if (existingUser.getRole() == Role.CLIENT) {
                log.info("👤 Utilisateur déjà CLIENT, réutilisation ID: {}", existingUser.getId());
                return existingUser;
            }
            
            // Si PROSPECT → on réutilise
            if (existingUser.getRole() == Role.PROSPECT) {
                log.info("👤 Utilisateur PROSPECT existant, réutilisation ID: {}", existingUser.getId());
                return existingUser;
            }
            
            // Autre rôle (ADMIN, etc.) → on garde
            log.info("👤 Utilisateur avec rôle {} trouvé, réutilisation", existingUser.getRole());
            return existingUser;
        })
        .orElseGet(() -> {
            // ✅ CRÉER NOUVEL UTILISATEUR EN PROSPECT
            UserCreateRequest userRequest = new UserCreateRequest();
            userRequest.setNom(demande.getProspectNom());
            userRequest.setPrenom(demande.getProspectPrenom());
            userRequest.setEmail(demande.getProspectEmail());
            userRequest.setPassword(UUID.randomUUID().toString());
            userRequest.setRole(Role.PROSPECT); // ← Créé en PROSPECT
            UserDto newUser = userService.createUser(userRequest);
            
            log.info("✅ Nouveau PROSPECT créé: {}", newUser.getEmail());
            
            return userService.findById(newUser.getId())
                .orElseThrow(() -> new DemandeValidationException("Erreur création utilisateur"));
        });
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

        demande.setStatut(DemandeStatus.REJETEE);
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
