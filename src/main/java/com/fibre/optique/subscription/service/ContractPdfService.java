package com.fibre.optique.subscription.service;

import com.fibre.optique.billing.service.PdfUtils;
import com.fibre.optique.subscription.entity.Abonnement;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Generates professional contract PDFs using OpenPDF + shared PdfUtils helpers.
 */
@Service
public class ContractPdfService {

    private static final Logger log = LoggerFactory.getLogger(
        ContractPdfService.class
    );
    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_DATE =
        DateTimeFormatter.BASIC_ISO_DATE;

    private static final Color GREEN_OK = new Color(27, 140, 48);

    @Value("${app.storage.contracts-dir:./data/contracts}")
    private String contractsDir;

    // ── Public API ────────────────────────────────────────────────────────────

    public String generate(Abonnement abonnement) throws IOException {
        Path dir = Paths.get(contractsDir);
        Files.createDirectories(dir);

        String fileName = String.format(
            "CONTRAT-%d-%s.pdf",
            abonnement.getId(),
            LocalDate.now().format(FILE_DATE)
        );
        Path filePath = dir.resolve(fileName);

        Document doc = new Document(PageSize.A4, 45, 45, 45, 60);
        try {
            PdfWriter writer = PdfWriter.getInstance(
                doc,
                new FileOutputStream(filePath.toFile())
            );
            writer.setPageEvent(
                new PdfUtils.FooterEvent(
                    "Fibre Optique Platform  •  Contrat d'abonnement  •  Document généré automatiquement"
                )
            );
            doc.open();

            // ── Header ───────────────────────────────────────────────────────
            PdfUtils.addHeader(
                doc,
                writer,
                "CONTRAT D'ABONNEMENT",
                "N° " + abonnement.getId()
            );
            doc.add(Chunk.NEWLINE);

            // ── Introduction banner ───────────────────────────────────────────
            addIntroBanner(doc, abonnement);
            doc.add(Chunk.NEWLINE);

            // ── Two-column info block ─────────────────────────────────────────
            PdfPTable infoGrid = new PdfPTable(2);
            infoGrid.setWidthPercentage(100);
            infoGrid.setSpacingAfter(14);

            PdfUtils.addInfoSection(infoGrid, "SOUSCRIPTEUR", new String[][] {
                { "Prénom", abonnement.getClient().getPrenom() },
                { "Nom", abonnement.getClient().getNom() },
                { "Email", abonnement.getClient().getEmail() },
            });

            PdfUtils.addInfoSection(
                infoGrid,
                "OFFRE SOUSCRITE",
                new String[][] {
                    { "Offre", abonnement.getOffre().getNom() },
                    {
                        "Technologie",
                        abonnement.getOffre().getTechnologie().name(),
                    },
                    {
                        "Débit ↓",
                        abonnement.getOffre().getDebitDescendant() + " Mbps",
                    },
                    {
                        "Débit ↑",
                        abonnement.getOffre().getDebitMontant() + " Mbps",
                    },
                }
            );

            doc.add(infoGrid);

            // ── Duration table ────────────────────────────────────────────────
            PdfUtils.addSectionTitle(doc, "DURÉE & ENGAGEMENT");

            PdfPTable duration = new PdfPTable(new float[] { 3f, 3f, 2f, 2f });
            duration.setWidthPercentage(100);
            duration.setSpacingAfter(14);

            PdfUtils.addTableHeader(
                duration,
                "Date de début",
                "Date de fin",
                "Engagement",
                "Durée"
            );
            PdfUtils.addTableRow(
                duration,
                0,
                abonnement.getDateDebut().format(DATE_FMT),
                abonnement.getDateFin() != null
                    ? abonnement.getDateFin().format(DATE_FMT)
                    : "Indéterminée",
                abonnement
                    .getOffre()
                    .getTypeEngagement()
                    .name()
                    .replace('_', ' '),
                abonnement.getOffre().getDureeMois() + " mois"
            );
            doc.add(duration);

            // ── Pricing table ─────────────────────────────────────────────────
            PdfUtils.addSectionTitle(doc, "TARIFICATION MENSUELLE");

            BigDecimal tva =
                abonnement.getOffre().getTauxTVA() != null
                    ? abonnement.getOffre().getTauxTVA()
                    : BigDecimal.ZERO;
            BigDecimal prixTTC = abonnement
                .getOffre()
                .getPrixHT()
                .multiply(
                    BigDecimal.ONE.add(
                        tva.divide(
                            new BigDecimal("100"),
                            4,
                            RoundingMode.HALF_UP
                        )
                    )
                )
                .setScale(2, RoundingMode.HALF_UP);

            PdfPTable pricing = new PdfPTable(new float[] { 4f, 2f, 2f, 2f });
            pricing.setWidthPercentage(100);
            pricing.setSpacingAfter(14);

            PdfUtils.addTableHeader(
                pricing,
                "Offre",
                "Prix HT",
                "TVA",
                "Prix TTC"
            );
            PdfUtils.addTableRow(
                pricing,
                0,
                abonnement.getOffre().getNom(),
                String.format("%.2f MAD", abonnement.getOffre().getPrixHT()),
                tva.stripTrailingZeros().toPlainString() + " %",
                String.format("%.2f MAD", prixTTC)
            );
            doc.add(pricing);

            // ── Conditions ────────────────────────────────────────────────────
            addConditionsBlock(doc, abonnement);

            // ── Signature block ───────────────────────────────────────────────
            doc.add(Chunk.NEWLINE);
            addSignatureBlock(doc);
        } catch (Exception e) {
            log.error(
                "PDF generation failed for contract {}: {}",
                abonnement.getId(),
                e.getMessage(),
                e
            );
            throw new IOException("Failed to generate PDF", e);
        } finally {
            if (doc.isOpen()) doc.close();
        }

        log.info("Contract PDF generated: {}", filePath.toAbsolutePath());
        return "contracts/" + fileName;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void addIntroBanner(Document doc, Abonnement a)
        throws DocumentException {
        Font boldFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD,
            9,
            PdfUtils.NAVY
        );
        Font normalFont = FontFactory.getFont(
            FontFactory.HELVETICA,
            9,
            PdfUtils.TEXT_DARK
        );
        Font statusFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD,
            9,
            GREEN_OK
        );

        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingAfter(4);

        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(PdfUtils.ACCENT_BLUE);
        c.setBorderColor(PdfUtils.NAVY_LIGHT);
        c.setBorderWidth(0.8f);
        c.setPadding(10);

        Paragraph intro = new Paragraph();
        intro.add(new Chunk("Contrat N° " + a.getId(), boldFont));
        intro.add(
            new Chunk(
                "   —   signé le " + LocalDate.now().format(DATE_FMT),
                normalFont
            )
        );
        c.addElement(intro);

        Paragraph status = new Paragraph(
            "Statut : " + a.getStatut().name(),
            statusFont
        );
        status.setSpacingBefore(4);
        c.addElement(status);

        t.addCell(c);
        doc.add(t);
    }

    private void addConditionsBlock(Document doc, Abonnement a)
        throws DocumentException {
        PdfUtils.addSectionTitle(doc, "CONDITIONS GÉNÉRALES (RÉSUMÉ)");

        Font f = FontFactory.getFont(
            FontFactory.HELVETICA,
            8,
            new Color(60, 60, 60)
        );
        String engagement = a
            .getOffre()
            .getTypeEngagement()
            .name()
            .replace('_', ' ');

        String[] clauses = {
            "1. Le présent contrat est conclu entre FIBRE OPTIQUE PLATFORM et le souscripteur identifié ci-dessus.",
            "2. L'offre souscrite est : " +
                a.getOffre().getNom() +
                " — technologie " +
                a.getOffre().getTechnologie().name() +
                " — engagement " +
                engagement +
                ".",
            "3. Le montant mensuel TTC est facturé au début de chaque période d'abonnement.",
            "4. Toute résiliation avant la fin de l'engagement peut entraîner des frais conformément" +
                " aux conditions tarifaires en vigueur.",
            "5. Le souscripteur bénéficie d'un délai de rétractation de 14 jours à compter de la signature.",
            "6. En cas de litige, les parties s'engagent à rechercher une solution amiable avant tout recours judiciaire.",
        };

        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingAfter(12);

        for (int i = 0; i < clauses.length; i++) {
            PdfPCell c = new PdfPCell(new Phrase(clauses[i], f));
            c.setBackgroundColor(
                i % 2 == 0 ? PdfUtils.WHITE : PdfUtils.GREY_ROW
            );
            c.setBorderColor(PdfUtils.GREY_BORDER);
            c.setBorderWidth(0.4f);
            c.setPadding(6);
            t.addCell(c);
        }

        doc.add(t);
    }

    private void addSignatureBlock(Document doc) throws DocumentException {
        PdfUtils.addSectionTitle(doc, "SIGNATURES");

        Font labelFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD,
            9,
            PdfUtils.NAVY
        );
        Font smallFont = FontFactory.getFont(
            FontFactory.HELVETICA,
            8,
            new Color(100, 100, 100)
        );

        PdfPTable t = new PdfPTable(new float[] { 1f, 1f });
        t.setWidthPercentage(100);
        t.setSpacingAfter(10);

        // Client cell
        PdfPCell clientCell = new PdfPCell();
        clientCell.setBorderColor(PdfUtils.GREY_BORDER);
        clientCell.setBorderWidth(0.5f);
        clientCell.setPadding(12);
        clientCell.setMinimumHeight(80);
        Paragraph clientTitle = new Paragraph("Signature du client", labelFont);
        clientTitle.setSpacingAfter(4);
        clientCell.addElement(clientTitle);
        clientCell.addElement(new Paragraph("Lu et approuvé", smallFont));
        clientCell.addElement(new Paragraph(" ", smallFont));
        clientCell.addElement(
            new Paragraph("Date : ___/___/______", smallFont)
        );
        t.addCell(clientCell);

        // Operator cell
        PdfPCell opCell = new PdfPCell();
        opCell.setBorderColor(PdfUtils.GREY_BORDER);
        opCell.setBorderWidth(0.5f);
        opCell.setPadding(12);
        opCell.setMinimumHeight(80);
        Paragraph opTitle = new Paragraph(
            "Cachet & signature opérateur",
            labelFont
        );
        opTitle.setSpacingAfter(4);
        opCell.addElement(opTitle);
        opCell.addElement(
            new Paragraph("Pour FIBRE OPTIQUE PLATFORM", smallFont)
        );
        opCell.addElement(new Paragraph(" ", smallFont));
        opCell.addElement(
            new Paragraph(
                "Date : " + LocalDate.now().format(DATE_FMT),
                smallFont
            )
        );
        t.addCell(opCell);

        doc.add(t);

        Font noticeFont = FontFactory.getFont(
            FontFactory.HELVETICA_OBLIQUE,
            7,
            new Color(120, 120, 120)
        );
        Paragraph notice = new Paragraph(
            "Ce document a valeur contractuelle une fois signé par les deux parties. Conservez-en un exemplaire.",
            noticeFont
        );
        notice.setAlignment(Element.ALIGN_CENTER);
        doc.add(notice);
    }
}
