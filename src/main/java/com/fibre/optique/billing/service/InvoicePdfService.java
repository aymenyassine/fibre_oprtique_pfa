package com.fibre.optique.billing.service;

import com.fibre.optique.billing.entity.Facture;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Generates invoice PDF files using OpenPDF.
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

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
            document.open();
            Font monoFont = FontFactory.getFont(FontFactory.COURIER, 10);

            String text = buildInvoiceText(facture);
            for (String line : text.split("\n")) {
                Paragraph p = new Paragraph(line, monoFont);
                p.setLeading(14f);
                document.add(p);
            }
        } catch (Exception e) {
            log.error("PDF generation failed for invoice {}: {}", facture.getReference(), e.getMessage());
            throw new IOException("Failed to generate PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
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
        if (!Files.exists(filePath)) {
            filePath = Paths.get("./data").resolve(pdfStorageKey);
        }
        return Files.readAllBytes(filePath);
    }

    // -------------------------------------------------------------------------

    private String buildInvoiceText(Facture f) {
        return """
                ============================================================
                                      FACTURE
                ============================================================

                Reference    : %s
                Date emission: %s
                Date echeance: %s
                Statut       : %s

                --- CLIENT -------------------------------------------------
                Nom & Prenom : %s
                Email        : %s

                --- ABONNEMENT ---------------------------------------------
                Offre        : %s

                --- MONTANTS -----------------------------------------------
                Montant HT   : %.2f EUR
                Montant TTC  : %.2f EUR

                --- PAIEMENT -----------------------------------------------
                Veuillez regler avant le %s.
                ____________________________________________________________
                Fibre Optique Platform  -  Document genere automatiquement
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
