package com.fibre.optique.billing.service;

import com.fibre.optique.billing.entity.Facture;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Generates professional invoice PDFs using OpenPDF + shared PdfUtils helpers.
 */
@Service
public class InvoicePdfService {

    private static final Logger log = LoggerFactory.getLogger(
            InvoicePdfService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Status colours
    private static final Color GREEN_PAID = new Color(27, 140, 48);
    private static final Color RED_LATE = new Color(198, 40, 40);

    @Value("${app.storage.invoices-dir:./data/invoices}")
    private String invoicesDir;

    // ── Public API ────────────────────────────────────────────────────────────

    public String generate(Facture facture) throws IOException {
        Path dir = Paths.get(invoicesDir);
        Files.createDirectories(dir);

        String fileName = facture.getReference() + ".pdf";
        Path filePath = dir.resolve(fileName);

        Document doc = new Document(PageSize.A4, 45, 45, 45, 60);
        try {
            PdfWriter writer = PdfWriter.getInstance(
                    doc,
                    new FileOutputStream(filePath.toFile()));
            writer.setPageEvent(
                    new PdfUtils.FooterEvent(
                            "Fibre Optique Platform  •  Document généré automatiquement"));
            doc.open();

            // ── Header ───────────────────────────────────────────────────────
            PdfUtils.addHeader(doc, writer, "FACTURE", facture.getReference());
            doc.add(Chunk.NEWLINE);

            // ── Status banner ────────────────────────────────────────────────
            addStatusBanner(doc, facture.getStatut().toString());
            doc.add(Chunk.NEWLINE);

            // ── Two-column info block ────────────────────────────────────────
            PdfPTable infoGrid = new PdfPTable(2);
            infoGrid.setWidthPercentage(100);
            infoGrid.setSpacingAfter(14);

            PdfUtils.addInfoSection(
                    infoGrid,
                    "INFORMATIONS FACTURE",
                    new String[][] {
                            { "Référence", facture.getReference() },
                            {
                                    "Date d'émission",
                                    facture.getDateEmission().format(DATE_FMT),
                            },
                            {
                                    "Date d'échéance",
                                    facture.getDateEcheance().format(DATE_FMT),
                            },
                            {
                                    "Période",
                                    facture.getPeriodeDebut().format(DATE_FMT) +
                                            "  →  " +
                                            facture.getPeriodeFin().format(DATE_FMT),
                            },
                    });

            PdfUtils.addInfoSection(infoGrid, "CLIENT", new String[][] {
                    {
                            "Nom",
                            facture.getClient().getPrenom() +
                                    " " +
                                    facture.getClient().getNom(),
                    },
                    { "Email", facture.getClient().getEmail() },
                    { "Offre", facture.getAbonnement().getOffre().getNom() },
            });

            doc.add(infoGrid);

            // ── Line-items table ─────────────────────────────────────────────
            PdfUtils.addSectionTitle(doc, "DÉTAIL DE LA FACTURE");

            PdfPTable items = new PdfPTable(new float[] { 5f, 2f, 2f, 2f });
            items.setWidthPercentage(100);
            items.setSpacingAfter(14);

            PdfUtils.addTableHeader(
                    items,
                    "Description",
                    "Qté",
                    "Prix HT",
                    "Montant HT");
            PdfUtils.addTableRow(
                    items,
                    0,
                    "Abonnement " + facture.getAbonnement().getOffre().getNom(),
                    "1",
                    String.format("%.2f MAD", facture.getMontantHT()),
                    String.format("%.2f MAD", facture.getMontantHT()));
            doc.add(items);

            // ── Totals ───────────────────────────────────────────────────────
            addTotalsTable(doc, facture);

            // ── Payment notice ───────────────────────────────────────────────
            doc.add(Chunk.NEWLINE);
            addNoticeBox(
                    doc,
                    "Veuillez régler cette facture avant le " +
                            facture.getDateEcheance().format(DATE_FMT) +
                            ".");
        } catch (Exception e) {
            log.error(
                    "PDF generation failed for invoice {}: {}",
                    facture.getReference(),
                    e.getMessage(),
                    e);
            throw new IOException("Failed to generate PDF", e);
        } finally {
            if (doc.isOpen())
                doc.close();
        }

        log.info("Invoice PDF generated: {}", filePath.toAbsolutePath());
        return "invoices/" + fileName;
    }

    public byte[] read(String pdfStorageKey) throws IOException {
        Path p = Paths.get(pdfStorageKey);
        if (!Files.exists(p))
            p = Paths.get(".").resolve(pdfStorageKey);
        if (!Files.exists(p))
            p = Paths.get("./data").resolve(pdfStorageKey);
        return Files.readAllBytes(p);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void addStatusBanner(Document doc, String statut)
            throws DocumentException {
        Color bg, fg;
        switch (statut) {
            case "PAYEE" -> {
                bg = new Color(232, 245, 233);
                fg = GREEN_PAID;
            }
            case "EN_RETARD" -> {
                bg = new Color(255, 235, 238);
                fg = RED_LATE;
            }
            case "ANNULEE" -> {
                bg = new Color(245, 245, 245);
                fg = new Color(97, 97, 97);
            }
            default -> {
                bg = PdfUtils.ACCENT_BLUE;
                fg = PdfUtils.NAVY;
            }
        }
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, fg);
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingAfter(4);
        PdfPCell c = new PdfPCell(new Phrase("Statut : " + statut, f));
        c.setBackgroundColor(bg);
        c.setBorderColor(fg);
        c.setBorderWidth(1f);
        c.setPadding(8);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
        doc.add(t);
    }

    private void addTotalsTable(Document doc, Facture f)
            throws DocumentException {
        PdfUtils.addSectionTitle(doc, "RÉCAPITULATIF");

        PdfPTable t = new PdfPTable(new float[] { 4f, 2f });
        t.setWidthPercentage(55);
        t.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.setSpacingAfter(10);

        Font labelFont = FontFactory.getFont(
                FontFactory.HELVETICA,
                9,
                PdfUtils.TEXT_DARK);
        Font totalFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                10,
                Color.WHITE);

        addAmountRow(
                t,
                "Montant HT",
                String.format("%.2f MAD", f.getMontantHT()),
                labelFont);
        addAmountRow(
                t,
                "TVA (" +
                        f.getTauxTva().stripTrailingZeros().toPlainString() +
                        " %)",
                String.format(
                        "%.2f MAD",
                        f.getMontantTTC().subtract(f.getMontantHT())),
                labelFont);

        // TTC highlighted row
        PdfPCell ttcLabel = new PdfPCell(new Phrase("TOTAL TTC", totalFont));
        ttcLabel.setBackgroundColor(PdfUtils.NAVY);
        ttcLabel.setBorderColor(PdfUtils.NAVY);
        ttcLabel.setBorderWidth(0.5f);
        ttcLabel.setPadding(8);
        t.addCell(ttcLabel);

        PdfPCell ttcVal = new PdfPCell(
                new Phrase(String.format("%.2f MAD", f.getMontantTTC()), totalFont));
        ttcVal.setBackgroundColor(PdfUtils.NAVY);
        ttcVal.setBorderColor(PdfUtils.NAVY);
        ttcVal.setBorderWidth(0.5f);
        ttcVal.setPadding(8);
        ttcVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(ttcVal);

        doc.add(t);
    }

    private void addAmountRow(
            PdfPTable t,
            String label,
            String value,
            Font base) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, base));
        lbl.setBackgroundColor(PdfUtils.WHITE);
        lbl.setBorderColor(PdfUtils.GREY_BORDER);
        lbl.setBorderWidth(0.4f);
        lbl.setPadding(6);
        t.addCell(lbl);

        PdfPCell val = new PdfPCell(new Phrase(value, base));
        val.setBackgroundColor(PdfUtils.WHITE);
        val.setBorderColor(PdfUtils.GREY_BORDER);
        val.setBorderWidth(0.4f);
        val.setPadding(6);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(val);
    }

    private void addNoticeBox(Document doc, String message)
            throws DocumentException {
        Font f = FontFactory.getFont(
                FontFactory.HELVETICA_OBLIQUE,
                9,
                new Color(74, 74, 74));
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase(message, f));
        c.setBackgroundColor(new Color(255, 253, 231));
        c.setBorderColor(new Color(249, 168, 37));
        c.setBorderWidth(0.8f);
        c.setPadding(8);
        t.addCell(c);
        doc.add(t);
    }
}
