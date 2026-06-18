package com.fibre.optique.billing.service;

import com.fibre.optique.billing.entity.Facture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Generates invoice PDF files.
 *
 * <p>Currently writes a plain-text representation (no external library required).
 * Replace {@link #buildInvoiceText} body with iText / OpenPDF calls for production.</p>
 */
@Service
public class InvoicePdfService {

    private static final Logger log = LoggerFactory.getLogger(InvoicePdfService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${app.storage.invoices-dir:./data/invoices}")
    private String invoicesDir;

    /**
     * Generates an invoice PDF and returns its storage key.
     *
     * @param facture fully loaded Facture (client + abonnement + offre must be initialised)
     * @return relative storage key, e.g. "invoices/FAC-2026-0001.pdf"
     */
    public String generate(Facture facture) throws IOException {
        Path dir = Paths.get(invoicesDir);
        Files.createDirectories(dir);

        String fileName = facture.getReference() + ".pdf";
        Path filePath = dir.resolve(fileName);

        try (OutputStream out = new FileOutputStream(filePath.toFile())) {
            out.write(buildInvoiceText(facture).getBytes(StandardCharsets.UTF_8));
        }

        log.info("Invoice PDF generated: {}", filePath.toAbsolutePath());
        return "invoices/" + fileName;
    }

    /**
     * Returns the raw bytes of an already-generated invoice PDF.
     */
    public byte[] read(String pdfStorageKey) throws IOException {
        Path filePath = Paths.get(pdfStorageKey);
        if (!Files.exists(filePath)) {
            filePath = Paths.get(".").resolve(pdfStorageKey);
        }
        return Files.readAllBytes(filePath);
    }

    // -------------------------------------------------------------------------

    private String buildInvoiceText(Facture f) {
        return """
                ============================================================
                                      FACTURE
                ============================================================

                Référence    : %s
                Date émission: %s
                Date échéance: %s
                Statut       : %s

                ── CLIENT ──────────────────────────────────────────────────
                Nom & Prénom : %s
                Email        : %s

                ── ABONNEMENT ──────────────────────────────────────────────
                Offre        : %s

                ── MONTANTS ────────────────────────────────────────────────
                Montant HT   : %.2f €
                Montant TTC  : %.2f €

                ── PAIEMENT ────────────────────────────────────────────────
                Veuillez régler avant le %s.
                ____________________________________________________________
                Fibre Optique Platform  —  Document généré automatiquement
                ============================================================
                """.formatted(
                f.getReference(),
                f.getDateEmission().format(DATE_FMT),
                f.getDateEcheance().format(DATE_FMT),
                f.getStatut(),
                f.getClient().getNom() + " " + f.getClient().getPrenom(),
                f.getClient().getEmail(),
                f.getAbonnement().getOffre().getNom(),
                f.getMontantHT(),
                f.getMontantTTC(),
                f.getDateEcheance().format(DATE_FMT)
        );
    }
}
