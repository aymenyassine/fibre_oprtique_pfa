package com.fibre.optique.subscription.service;

import com.fibre.optique.offer.entity.Offre;
import com.fibre.optique.offer.service.OfferService;
import com.fibre.optique.subscription.dto.AbonnementDto;
import com.fibre.optique.subscription.dto.ContratDto;
import com.fibre.optique.subscription.dto.SubscriptionRequest;
import com.fibre.optique.subscription.entity.Abonnement;
import com.fibre.optique.subscription.entity.AbonnementStatus;
import com.fibre.optique.subscription.entity.Contrat;
import com.fibre.optique.subscription.event.SubscriptionCreatedEvent;
import com.fibre.optique.subscription.event.SubscriptionStatusChangedEvent;
import com.fibre.optique.subscription.exception.ContratNotFoundException;
import com.fibre.optique.subscription.exception.SubscriptionNotFoundException;
import com.fibre.optique.subscription.exception.SubscriptionValidationException;
import com.fibre.optique.subscription.repository.AbonnementRepository;
import com.fibre.optique.subscription.repository.ContratRepository;
import com.fibre.optique.users.entity.User;
import com.fibre.optique.users.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final AbonnementRepository abonnementRepository;
    private final ContratRepository contratRepository;
    private final UserService userService;
    private final OfferService offerService;
    private final ContractPdfService contractPdfService;
    private final ApplicationEventPublisher eventPublisher;

    public SubscriptionService(AbonnementRepository abonnementRepository,
                                ContratRepository contratRepository,
                                UserService userService,
                                OfferService offerService,
                                ContractPdfService contractPdfService,
                                ApplicationEventPublisher eventPublisher) {
        this.abonnementRepository = abonnementRepository;
        this.contratRepository = contratRepository;
        this.userService = userService;
        this.offerService = offerService;
        this.contractPdfService = contractPdfService;
        this.eventPublisher = eventPublisher;
    }

    // =========================================================================
    // QUERIES
    // =========================================================================

    public List<AbonnementDto> getSubscriptionsByClient(Long clientId) {
        return abonnementRepository.findByClientId(clientId)
                .stream()
                .map(AbonnementDto::fromEntity)
                .toList();
    }

    public AbonnementDto getSubscriptionById(Long id) {
        return abonnementRepository.findByIdWithDetails(id)
                .map(AbonnementDto::fromEntity)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));
    }

    public List<AbonnementDto> getAllSubscriptions() {
        return abonnementRepository.findAll()
                .stream()
                .map(AbonnementDto::fromEntity)
                .toList();
    }

    /**
     * Exposes the raw entity — used by BillingService for invoice generation.
     */
    public List<Abonnement> getActiveSubscriptionEntities() {
        return abonnementRepository.findByStatut(AbonnementStatus.ACTIF);
    }

    /**
     * Returns the raw entity by id — used by BillingService.
     */
    public Abonnement getAbonnementEntityById(Long id) {
        return abonnementRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));
    }

    // =========================================================================
    // CREATION
    // =========================================================================

    /**
     * Subscribes a client to an offer.
     * <ol>
     *   <li>Validates no active subscription already exists for this client.</li>
     *   <li>Creates the {@link Abonnement} record.</li>
     *   <li>Generates the contract PDF via {@link ContractPdfService}.</li>
     *   <li>Saves the {@link Contrat} record.</li>
     *   <li>Publishes a {@link SubscriptionCreatedEvent} for async email notification.</li>
     * </ol>
     *
     * @param request clientId + offreId
     * @return full AbonnementDto
     */
    @Transactional
    public AbonnementDto createSubscription(SubscriptionRequest request) {
        // 1. Load client and offer (direct Java calls — no HTTP)
        User client = userService.findById(request.getClientId())
                .orElseThrow(() -> new SubscriptionValidationException(
                        "Client introuvable avec l'identifiant : " + request.getClientId()));

        if (abonnementRepository.existsByClientIdAndStatut(client.getId(), AbonnementStatus.ACTIF)) {
            throw new SubscriptionValidationException(
                    "Ce client possède déjà un abonnement actif. Veuillez d'abord le résilier.");
        }

        Offre offre = offerService.getOfferEntityById(request.getOffreId());

        if (!offre.getActif()) {
            throw new SubscriptionValidationException(
                    "L'offre sélectionnée n'est plus disponible : " + offre.getNom());
        }

        // 2. Compute end date from engagement duration
        LocalDate dateDebut = LocalDate.now();



        // 3. Persist subscription
        Abonnement abonnement = Abonnement.builder()
                .client(client)
                .offre(offre)
                .dateDebut(dateDebut)
                .dateFin(Abonnement.calculateEndDateFromOffer(offre, dateDebut))
                .statut(AbonnementStatus.ACTIF)
                .build();

        abonnement = abonnementRepository.save(abonnement);

        // 4. Generate contract PDF
        String pdfKey;
        try {
            pdfKey = contractPdfService.generate(abonnement);
        } catch (IOException e) {
            log.error("Failed to generate contract PDF for subscription {}", abonnement.getId(), e);
            pdfKey = "contracts/ERROR-" + abonnement.getId() + ".pdf";
        }

        Contrat contrat = Contrat.builder()
                .abonnement(abonnement)
                .pdfStorageKey(pdfKey)
                .build();
        contratRepository.save(contrat);

        // 5. Publish async event for email notification
        eventPublisher.publishEvent(new SubscriptionCreatedEvent(
                this,
                abonnement.getId(),
                client.getEmail(),
                client.getNom() + " " + client.getPrenom(),
                offre.getNom()
        ));

        return AbonnementDto.fromEntity(abonnement);
    }

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    /**
     * Suspends an active subscription (payment default, client request, etc.)
     */
    @Transactional
    public AbonnementDto suspendSubscription(Long id) {
        Abonnement abonnement = loadAbonnement(id);

        if (abonnement.getStatut() != AbonnementStatus.ACTIF) {
            throw new SubscriptionValidationException(
                    "Seul un abonnement ACTIF peut être suspendu. Statut actuel : " + abonnement.getStatut());
        }

        abonnement.setStatut(AbonnementStatus.SUSPENDU);
        abonnement = abonnementRepository.save(abonnement);

        eventPublisher.publishEvent(new SubscriptionStatusChangedEvent(
                this, abonnement.getId(),
                abonnement.getClient().getEmail(),
                abonnement.getClient().getNom(),
                AbonnementStatus.SUSPENDU));

        return AbonnementDto.fromEntity(abonnement);
    }

    /**
     * Terminates a subscription definitively.
     */
    @Transactional
    public AbonnementDto terminateSubscription(Long id) {
        Abonnement abonnement = loadAbonnement(id);

        if (abonnement.getStatut() == AbonnementStatus.RESILIE) {
            throw new SubscriptionValidationException("Cet abonnement est déjà résilié.");
        }

        abonnement.setStatut(AbonnementStatus.RESILIE);
        abonnement.setDateFin(LocalDate.now());
        abonnement = abonnementRepository.save(abonnement);

        eventPublisher.publishEvent(new SubscriptionStatusChangedEvent(
                this, abonnement.getId(),
                abonnement.getClient().getEmail(),
                abonnement.getClient().getNom(),
                AbonnementStatus.RESILIE));

        return AbonnementDto.fromEntity(abonnement);
    }

    /**
     * Reactivates a suspended subscription.
     */
    @Transactional
    public AbonnementDto reactivateSubscription(Long id) {
        Abonnement abonnement = loadAbonnement(id);

        if (abonnement.getStatut() != AbonnementStatus.SUSPENDU) {
            throw new SubscriptionValidationException(
                    "Seul un abonnement SUSPENDU peut être réactivé. Statut actuel : " + abonnement.getStatut());
        }

        abonnement.setStatut(AbonnementStatus.ACTIF);
        abonnement = abonnementRepository.save(abonnement);

        return AbonnementDto.fromEntity(abonnement);
    }

    /**
     * Switches the offer on an active subscription.
     * Records the change timestamp and regenerates the contract PDF.
     */
    @Transactional
    public AbonnementDto changeOffer(Long id, Long newOffreId) {
        Abonnement abonnement = loadAbonnement(id);

        if (abonnement.getStatut() != AbonnementStatus.ACTIF) {
            throw new SubscriptionValidationException(
                    "Le changement d'offre n'est possible que sur un abonnement ACTIF.");
        }

        Offre newOffre = offerService.getOfferEntityById(newOffreId);
        if (!newOffre.getActif()) {
            throw new SubscriptionValidationException(
                    "L'offre cible n'est plus disponible : " + newOffre.getNom());
        }

        abonnement.setOffre(newOffre);
        abonnement.setDateChangementOffre(Instant.now());

        // Recompute end date from new offer engagement (null = open-ended)
        abonnement.setDateFin(newOffre.getDureeMois() > 0
                ? LocalDate.now().plusMonths(newOffre.getDureeMois())
                : null);

        abonnement = abonnementRepository.save(abonnement);

        // Regenerate contract PDF for the new offer
        try {
            String pdfKey = contractPdfService.generate(abonnement);
            contratRepository.findByAbonnementId(abonnement.getId())
                    .ifPresent(c -> {
                        c.setPdfStorageKey(pdfKey);
                        contratRepository.save(c);
                    });
        } catch (IOException e) {
            log.error("Failed to regenerate contract PDF after offer change for subscription {}", id, e);
        }

        return AbonnementDto.fromEntity(abonnement);
    }

    // =========================================================================
    // CONTRACT DOWNLOAD
    // =========================================================================

    /**
     * Returns the contract metadata for a subscription.
     * The caller (controller) serves the actual file from the storage key.
     */
    public ContratDto getContrat(Long abonnementId) {
        return contratRepository.findByAbonnementId(abonnementId)
                .map(ContratDto::fromEntity)
                .orElseThrow(() -> new ContratNotFoundException(abonnementId));
    }

    /**
     * Returns the raw contract file bytes for download.
     */
    public byte[] downloadContrat(Long abonnementId) throws IOException {
        Contrat contrat = contratRepository.findByAbonnementId(abonnementId)
                .orElseThrow(() -> new ContratNotFoundException(abonnementId));

        java.nio.file.Path filePath = java.nio.file.Paths.get(contrat.getPdfStorageKey());
        if (!java.nio.file.Files.exists(filePath)) {
            // Try resolving relative to working directory
            filePath = java.nio.file.Paths.get(".").resolve(contrat.getPdfStorageKey());
        }

        return java.nio.file.Files.readAllBytes(filePath);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Abonnement loadAbonnement(Long id) {
        return abonnementRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));
    }
}
