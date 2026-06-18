package com.fibre.optique.offer.service;

import com.fibre.optique.offer.dto.*;
import com.fibre.optique.offer.entity.Offre;
import com.fibre.optique.offer.entity.Technologie;
import com.fibre.optique.offer.entity.TypeEngagement;
import com.fibre.optique.offer.exception.OfferNotFoundException;
import com.fibre.optique.offer.exception.OfferValidationException;
import com.fibre.optique.offer.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    // =========================================================================
    // PUBLIC CATALOGUE
    // =========================================================================

    /**
     * Returns all active offers — accessible publicly without authentication.
     *
     * @param technologie   optional technology filter (FTTH / FTTB)
     * @param typeEngagement optional engagement type filter
     * @return list of active offer DTOs
     */
    public List<OfferDto> getActiveOffers(Technologie technologie, TypeEngagement typeEngagement) {
        List<Offre> offres;

        if (technologie != null && typeEngagement != null) {
            offres = offerRepository.findByActifTrueAndTechnologieAndTypeEngagement(technologie, typeEngagement);
        } else if (technologie != null) {
            offres = offerRepository.findByActifTrueAndTechnologie(technologie);
        } else if (typeEngagement != null) {
            offres = offerRepository.findByActifTrueAndTypeEngagement(typeEngagement);
        } else {
            offres = offerRepository.findByActifTrue();
        }

        return offres.stream().map(OfferDto::fromEntity).toList();
    }

    /**
     * Returns ALL offers including inactive ones — for admin management.
     */
    public List<OfferDto> getAllOffers() {
        return offerRepository.findAll()
                .stream()
                .map(OfferDto::fromEntity)
                .toList();
    }

    /**
     * Returns a full OfferDto by id — throws if not found.
     */
    public OfferDto getOfferDtoById(Long id) {
        return offerRepository.findById(id)
                .map(OfferDto::fromEntity)
                .orElseThrow(() -> new OfferNotFoundException(id));
    }

    /**
     * Returns the raw entity — used by other modules (e.g. SubscriptionService)
     * for JPA association without needing a DTO.
     */
    public Offre getOfferEntityById(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));
    }

    /**
     * Summary projection — lightweight, used in subscription / billing context.
     */
    public OfferSummaryDto getOfferSummaryById(Long id) {
        return offerRepository.findById(id)
                .map(OfferSummaryDto::fromEntity)
                .orElseThrow(() -> new OfferNotFoundException(id));
    }

    // =========================================================================
    // ADMIN CRUD
    // =========================================================================

    @Transactional
    public OfferDto createOffer(OfferRequest request) {
        if (offerRepository.existsByNomIgnoreCase(request.getNom())) {
            throw new OfferValidationException("Une offre avec ce nom existe déjà : " + request.getNom());
        }

        validateEngagementCoherence(request.getTypeEngagement(), request.getDureeMois());

        Offre offre = Offre.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .debitMontant(request.getDebitMontant())
                .debitDescendant(request.getDebitDescendant())
                .prixHT(request.getPrixHT())
                .tauxTVA(request.getTauxTVA() != null ? request.getTauxTVA() : new BigDecimal("20.00"))
                .typeEngagement(request.getTypeEngagement())
                .dureeMois(resolveDureeMois(request.getTypeEngagement(), request.getDureeMois()))
                .technologie(request.getTechnologie())
                .actif(request.getActif() != null ? request.getActif() : true)
                .build();

        return OfferDto.fromEntity(offerRepository.save(offre));
    }

    @Transactional
    public OfferDto updateOffer(Long id, OfferUpdateRequest request) {
        Offre offre = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));

        // Apply only non-null fields (partial update)
        if (request.getNom() != null) {
            if (!offre.getNom().equalsIgnoreCase(request.getNom())
                    && offerRepository.existsByNomIgnoreCase(request.getNom())) {
                throw new OfferValidationException("Une offre avec ce nom existe déjà : " + request.getNom());
            }
            offre.setNom(request.getNom());
        }
        if (request.getDescription() != null) offre.setDescription(request.getDescription());
        if (request.getDebitMontant() != null) offre.setDebitMontant(request.getDebitMontant());
        if (request.getDebitDescendant() != null) offre.setDebitDescendant(request.getDebitDescendant());
        if (request.getPrixHT() != null) offre.setPrixHT(request.getPrixHT());
        if (request.getTauxTVA() != null) offre.setTauxTVA(request.getTauxTVA());
        if (request.getTechnologie() != null) offre.setTechnologie(request.getTechnologie());
        if (request.getActif() != null) offre.setActif(request.getActif());

        if (request.getTypeEngagement() != null) {
            offre.setTypeEngagement(request.getTypeEngagement());
            offre.setDureeMois(resolveDureeMois(
                    request.getTypeEngagement(),
                    request.getDureeMois() != null ? request.getDureeMois() : offre.getDureeMois()));
        } else if (request.getDureeMois() != null) {
            offre.setDureeMois(request.getDureeMois());
        }

        return OfferDto.fromEntity(offerRepository.save(offre));
    }

    /**
     * Soft-deactivates an offer. Active subscriptions are not affected —
     * the offer remains readable but no longer appears in the public catalogue.
     */
    @Transactional
    public void deactivateOffer(Long id) {
        Offre offre = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));
        offre.setActif(false);
        offerRepository.save(offre);
    }

    /**
     * Hard delete — use with caution; will fail if subscriptions reference this offer.
     */
    @Transactional
    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new OfferNotFoundException(id);
        }
        offerRepository.deleteById(id);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Ensures the engagement duration is consistent with the engagement type.
     */
    private void validateEngagementCoherence(TypeEngagement type, Integer dureeMois) {
        if (type == TypeEngagement.SANS_ENGAGEMENT && dureeMois != null && dureeMois > 0) {
            throw new OfferValidationException(
                    "Une offre sans engagement ne peut pas avoir une durée supérieure à 0 mois");
        }
    }

    /**
     * Derives the duree_mois from the engagement type when not explicitly supplied.
     */
    private int resolveDureeMois(TypeEngagement type, Integer requestedDuree) {
        if (requestedDuree != null) return requestedDuree;
        return switch (type) {
            case SANS_ENGAGEMENT -> 0;
            case DOUZE_MOIS -> 12;
            case VINGT_QUATRE_MOIS -> 24;
        };
    }
}
