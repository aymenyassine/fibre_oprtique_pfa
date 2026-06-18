package com.fibre.optique.subscription.service;

import com.fibre.optique.subscription.entity.Abonnement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates a contract PDF for a given subscription.
 *
 * <p>In development, this writes a plain-text file (.pdf extension) so no external
 * PDF library is required for the project to compile and run.
 * Replace the body of {@link #generate} with iText / OpenPDF calls when a real
 * PDF is needed for production.</p>
 */
@Service
public class ContractPdfService {

    private static final Logger log = LoggerFactory.getLogger(ContractPdfService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Base directory where contract files are stored. Configurable via application.yml. */
    @Value("${app.storage.contracts-dir:./data/contracts}")
    private String contractsDir;

    /**
     * Generates a contract document for the given subscription.
     *
     * @param abonnement fully loaded subscription (client + offer must be initialised)
     * @return relative storage key (e.g. "contracts/CONTRAT-42-20260618.pdf")
     * @throws IOException if the file cannot be written
     */
    public String generate(Abonnement abonnement) throws IOException {
        Path dir = Paths.get(contractsDir);
        Files.createDirectories(dir);

        String fileName = String.format("CONTRAT-%d-%s.pdf",
                abonnement.getId(),
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));

        Path filePath = dir.resolve(fileName);

        try (OutputStream out = new FileOutputStream(filePath.toFile())) {
            String content = buildContractText(abonnement);
            out.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        log.info("Contract PDF generated: {}", filePath.toAbsolutePath());

        // Return a relative storage key (not the full OS path)
        return "contracts/" + fileName;
    }

    // -------------------------------------------------------------------------

    private String buildContractText(Abonnement a) {
        BigDecimal tva = a.getOffre().getTauxTVA() != null
                ? a.getOffre().getTauxTVA() : BigDecimal.ZERO;
        BigDecimal prixTTC = a.getOffre().getPrixHT()
                .multiply(BigDecimal.ONE.add(tva.divide(new BigDecimal("100"))));

        return """
                ============================================================
                           CONTRAT D'ABONNEMENT FIBRE OPTIQUE
                ============================================================

                Numéro d'abonnement : %d
                Date de signature   : %s

                ── CLIENT ──────────────────────────────────────────────────
                Nom & Prénom : %s %s
                Email        : %s

                ── OFFRE ───────────────────────────────────────────────────
                Offre        : %s
                Technologie  : %s
                Débit down   : %d Mbps
                Débit up     : %d Mbps
                Engagement   : %s (%d mois)
                Prix HT      : %.2f €
                TVA          : %.2f %%
                Prix TTC     : %.2f €

                ── DURÉE ───────────────────────────────────────────────────
                Date de début : %s
                Date de fin   : %s

                ── SIGNATURE ───────────────────────────────────────────────
                Lu et approuvé par le client.

                ____________________________________________________________
                Fibre Optique Platform  —  Document généré automatiquement
                ============================================================
                """.formatted(
                a.getId(),
                LocalDate.now().format(DATE_FMT),
                a.getClient().getPrenom(), a.getClient().getNom(),
                a.getClient().getEmail(),
                a.getOffre().getNom(),
                a.getOffre().getTechnologie(),
                a.getOffre().getDebitDescendant(),
                a.getOffre().getDebitMontant(),
                a.getOffre().getTypeEngagement(), a.getOffre().getDureeMois(),
                a.getOffre().getPrixHT(),
                tva,
                prixTTC,
                a.getDateDebut().format(DATE_FMT),
                a.getDateFin() != null ? a.getDateFin().format(DATE_FMT) : "Indéterminée"
        );
    }
}
