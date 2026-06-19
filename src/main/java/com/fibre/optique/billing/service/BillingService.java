package com.fibre.optique.billing.service;

import com.fibre.optique.billing.dto.BillingStatsDto;
import com.fibre.optique.billing.dto.FactureDto;
import com.fibre.optique.billing.dto.MonthlyBillingStatsDto;
import com.fibre.optique.billing.dto.PaymentRequest;
import com.fibre.optique.billing.entity.Facture;
import com.fibre.optique.billing.entity.FactureStatus;
import com.fibre.optique.billing.event.InvoiceGeneratedEvent;
import com.fibre.optique.billing.event.InvoiceOverdueEvent;
import com.fibre.optique.billing.exception.BillingValidationException;
import com.fibre.optique.billing.exception.FactureNotFoundException;
import com.fibre.optique.billing.repository.FactureRepository;
import com.fibre.optique.subscription.entity.Abonnement;
import com.fibre.optique.subscription.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    /** Payment due: 30 days after emission */
    private static final int PAYMENT_DUE_DAYS = 30;

    private final FactureRepository factureRepository;
    private final SubscriptionService subscriptionService;
    private final InvoicePdfService invoicePdfService;
    private final ApplicationEventPublisher eventPublisher;

    public BillingService(FactureRepository factureRepository,
                           SubscriptionService subscriptionService,
                           InvoicePdfService invoicePdfService,
                           ApplicationEventPublisher eventPublisher) {
        this.factureRepository = factureRepository;
        this.subscriptionService = subscriptionService;
        this.invoicePdfService = invoicePdfService;
        this.eventPublisher = eventPublisher;
    }

    // =========================================================================
    // QUERIES
    // =========================================================================

    /** Invoices for the currently authenticated client. */
    public List<FactureDto> getMyInvoices(Long clientId) {
        return factureRepository.findByClientId(clientId)
                .stream()
                .map(FactureDto::fromEntity)
                .toList();
    }

    public FactureDto getById(Long id) {
        return factureRepository.findById(id)
                .map(FactureDto::fromEntity)
                .orElseThrow(() -> new FactureNotFoundException(id));
    }

    /** Paginated, filtered invoice list — admin / staff use. */
    public Page<FactureDto> search(Long clientId, FactureStatus statut,
                                    LocalDate from, LocalDate to, Pageable pageable) {
        return factureRepository.search(clientId, statut, from, to, pageable)
                .map(FactureDto::fromEntity);
    }

    /** Loads raw invoice bytes for download. */
    @Transactional
    public byte[] downloadInvoicePdf(Long id) throws IOException {
        Facture facture = loadEntity(id);
        String key = facture.getPdfStorageKey();
        if (key == null || !invoicePdfFileExists(key)) {
            key = invoicePdfService.generate(facture);
            facture.setPdfStorageKey(key);
            factureRepository.save(facture);
        }
        return invoicePdfService.read(key);
    }

    private boolean invoicePdfFileExists(String key) {
        if (key == null) return false;
        java.nio.file.Path filePath = java.nio.file.Paths.get(key);
        if (java.nio.file.Files.exists(filePath)) return true;
        if (java.nio.file.Files.exists(java.nio.file.Paths.get(".").resolve(key))) return true;
        return java.nio.file.Files.exists(java.nio.file.Paths.get("./data").resolve(key));
    }

    // =========================================================================
    // PAYMENT
    // =========================================================================

    /**
     * Records a payment against an invoice.
     * Validates that the paid amount matches the TTC total.
     */
    @Transactional
    public FactureDto recordPayment(Long id, PaymentRequest request) {
        Facture facture = loadEntity(id);

        if (facture.getStatut() == FactureStatus.PAYEE) {
            throw new BillingValidationException("Cette facture est déjà réglée : " + facture.getReference());
        }
        if (facture.getStatut() == FactureStatus.ANNULEE) {
            throw new BillingValidationException("Impossible de régler une facture annulée : " + facture.getReference());
        }

        // Tolerance of 1 cent to handle rounding
        if (request.getMontant().subtract(facture.getMontantTTC()).abs()
                .compareTo(new BigDecimal("0.01")) > 0) {
            throw new BillingValidationException(
                    "Le montant réglé (%.2f €) ne correspond pas au montant TTC de la facture (%.2f €)."
                            .formatted(request.getMontant(), facture.getMontantTTC()));
        }

        facture.setStatut(FactureStatus.PAYEE);
        facture.setDatePaiement(Instant.now());
        return FactureDto.fromEntity(factureRepository.save(facture));
    }

    /**
     * Cancels an invoice — only allowed if EN_ATTENTE or EN_RETARD.
     */
    @Transactional
    public FactureDto cancelInvoice(Long id) {
        Facture facture = loadEntity(id);

        if (facture.getStatut() == FactureStatus.PAYEE) {
            throw new BillingValidationException(
                    "Une facture déjà réglée ne peut pas être annulée : " + facture.getReference());
        }

        facture.setStatut(FactureStatus.ANNULEE);
        return FactureDto.fromEntity(factureRepository.save(facture));
    }

    // =========================================================================
    // STATS
    // =========================================================================

    public BillingStatsDto getStats() {
        List<Facture> all = factureRepository.findAll();

        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastOfMonth  = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        BigDecimal ca = all.stream()
                .filter(f -> f.getStatut() == FactureStatus.PAYEE
                        && !f.getDateEmission().isBefore(firstOfMonth)
                        && !f.getDateEmission().isAfter(lastOfMonth))
                .map(Facture::getMontantTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal impaye = all.stream()
                .filter(f -> f.getStatut() == FactureStatus.EN_ATTENTE
                        || f.getStatut() == FactureStatus.EN_RETARD)
                .map(Facture::getMontantTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BillingStatsDto.builder()
                .totalFactures(all.size())
                .facturesEnAttente(all.stream().filter(f -> f.getStatut() == FactureStatus.EN_ATTENTE).count())
                .facturesPayees(all.stream().filter(f -> f.getStatut() == FactureStatus.PAYEE).count())
                .facturesEnRetard(all.stream().filter(f -> f.getStatut() == FactureStatus.EN_RETARD).count())
                .facturesAnnulees(all.stream().filter(f -> f.getStatut() == FactureStatus.ANNULEE).count())
                .chiffreAffairesMoisCourant(ca)
                .montantImpayeTotal(impaye)
                .build();
    }

    public List<MonthlyBillingStatsDto> getMonthlyStats(int months) {
        List<Facture> all = factureRepository.findAll();
        List<MonthlyBillingStatsDto> stats = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            LocalDate start = monthDate.withDayOfMonth(1);
            LocalDate end = monthDate.withDayOfMonth(monthDate.lengthOfMonth());

            String monthLabel = getFrenchMonthLabel(monthDate);

            BigDecimal revenue = all.stream()
                    .filter(f -> f.getStatut() == FactureStatus.PAYEE
                            && !f.getDateEmission().isBefore(start)
                            && !f.getDateEmission().isAfter(end))
                    .map(Facture::getMontantTTC)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long invoicesCreated = all.stream()
                    .filter(f -> !f.getDateEmission().isBefore(start)
                            && !f.getDateEmission().isAfter(end))
                    .count();

            stats.add(MonthlyBillingStatsDto.builder()
                    .month(monthLabel)
                    .revenue(revenue)
                    .invoicesCreated(invoicesCreated)
                    .build());
        }
        return stats;
    }

    private String getFrenchMonthLabel(LocalDate date) {
        return switch (date.getMonthValue()) {
            case 1 -> "Jan";
            case 2 -> "Fév";
            case 3 -> "Mar";
            case 4 -> "Avr";
            case 5 -> "Mai";
            case 6 -> "Juin";
            case 7 -> "Juil";
            case 8 -> "Août";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Déc";
            default -> "";
        };
    }

    // =========================================================================
    // INTERNAL — called by BillingScheduler
    // =========================================================================

    /**
     * Generates one invoice per active subscription.
     * Called by {@code BillingScheduler} on the 1st of every month.
     */
    @Transactional
    public void generateMonthlyInvoices() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd   = today.withDayOfMonth(today.lengthOfMonth());

        List<Abonnement> activeSubscriptions = subscriptionService.getActiveSubscriptionEntities();
        log.info("Monthly billing: processing {} active subscriptions", activeSubscriptions.size());

        for (Abonnement abonnement : activeSubscriptions) {
            try {
                // Idempotency guard — don't double-bill in the same month
                if (factureRepository.existsByAbonnementIdAndDateEmissionBetween(
                        abonnement.getId(), monthStart, monthEnd)) {
                    log.debug("Invoice already exists for subscription {} this month, skipping.",
                            abonnement.getId());
                    continue;
                }

                Facture facture = buildInvoice(abonnement, today);
                facture = factureRepository.save(facture);

                // Generate PDF
                try {
                    String pdfKey = invoicePdfService.generate(facture);
                    facture.setPdfStorageKey(pdfKey);
                    factureRepository.save(facture);
                } catch (IOException e) {
                    log.error("PDF generation failed for invoice {}: {}", facture.getReference(), e.getMessage());
                }

                // Notify client
                eventPublisher.publishEvent(new InvoiceGeneratedEvent(
                        this,
                        facture.getId(),
                        facture.getClient().getEmail(),
                        facture.getClient().getNom(),
                        facture.getReference(),
                        facture.getMontantTTC()
                ));

            } catch (Exception e) {
                log.error("Failed to generate invoice for subscription {}: {}",
                        abonnement.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Marks overdue invoices as EN_RETARD and triggers payment reminder emails.
     * Called daily by {@code BillingScheduler}.
     */
    @Transactional
    public void markOverdueInvoices() {
        LocalDate today = LocalDate.now();

        List<Facture> overdue = factureRepository
                .findByStatutAndDateEcheanceBefore(FactureStatus.EN_ATTENTE, today);

        log.info("Overdue check: {} invoices to mark as EN_RETARD", overdue.size());

        for (Facture facture : overdue) {
            facture.setStatut(FactureStatus.EN_RETARD);
            factureRepository.save(facture);

            eventPublisher.publishEvent(new InvoiceOverdueEvent(
                    this,
                    facture.getId(),
                    facture.getClient().getEmail(),
                    facture.getClient().getNom(),
                    facture.getReference(),
                    facture.getMontantTTC()
            ));
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Facture loadEntity(Long id) {
        return factureRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new FactureNotFoundException(id));
    }

    private Facture buildInvoice(Abonnement abonnement, LocalDate emission) {
        BigDecimal prixHT = abonnement.getOffre().getPrixHT();
        BigDecimal tva    = abonnement.getOffre().getTauxTVA() != null
                ? abonnement.getOffre().getTauxTVA()
                : new BigDecimal("20.00");
        BigDecimal prixTTC = prixHT
                .multiply(BigDecimal.ONE.add(tva.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);

        String reference = buildReference(emission);

        return Facture.builder()
                .client(abonnement.getClient())
                .abonnement(abonnement)
                .reference(reference)
                .periodeDebut(emission.withDayOfMonth(1))
                .periodeFin(emission.withDayOfMonth(emission.lengthOfMonth()))
                .montantHT(prixHT)
                .tauxTva(tva)
                .montantTTC(prixTTC)
                .dateEmission(emission)
                .dateEcheance(emission.plusDays(PAYMENT_DUE_DAYS))
                .statut(FactureStatus.EN_ATTENTE)
                .build();
    }

    /**
     * Builds a human-readable unique reference: FAC-YYYY-NNNN
     * (thread-safe because this runs inside a @Transactional method).
     */
    private String buildReference(LocalDate date) {
        int year = date.getYear();
        long count = factureRepository.countByYear(year) + 1;
        return "FAC-%d-%04d".formatted(year, count);
    }
}
