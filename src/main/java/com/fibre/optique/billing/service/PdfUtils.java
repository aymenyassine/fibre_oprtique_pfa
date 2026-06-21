package com.fibre.optique.billing.service;

import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

import java.awt.Color;

/**
 * Shared PDF building utilities used by both InvoicePdfService and ContractPdfService.
 */
public final class PdfUtils {

    // ── Palette ───────────────────────────────────────────────────────────────
    public static final Color NAVY        = new Color(26,  35, 126);
    public static final Color NAVY_LIGHT  = new Color(57,  73, 171);
    public static final Color ACCENT_BLUE = new Color(227, 242, 253);
    public static final Color GREY_ROW    = new Color(245, 245, 250);
    public static final Color GREY_BORDER = new Color(189, 189, 189);
    public static final Color WHITE       = Color.WHITE;
    public static final Color TEXT_DARK   = new Color( 33,  33,  33);

    private PdfUtils() {}

    // ── Header ────────────────────────────────────────────────────────────────

    /**
     * Renders the full-width dark-blue header band with company name,
     * document type and reference number.
     */
    public static void addHeader(Document doc, PdfWriter writer,
                                 String docType, String ref)
            throws DocumentException {

        // Coloured band drawn behind the table (absolute positioning)
        PdfContentByte canvas = writer.getDirectContentUnder();
        float pageWidth  = doc.getPageSize().getWidth();
        float top        = doc.getPageSize().getHeight() - doc.topMargin() + 15;

        canvas.setColorFill(NAVY);
        canvas.rectangle(0, top - 70, pageWidth, 70);
        canvas.fill();

        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22,
                new Color(255, 255, 255));
        Font subFont     = FontFactory.getFont(FontFactory.HELVETICA, 11,
                new Color(179, 198, 255));
        Font docTypeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14,
                new Color(179, 198, 255));
        Font refFont     = FontFactory.getFont(FontFactory.HELVETICA, 10,
                new Color(200, 210, 255));

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{3f, 1f});
        header.setSpacingAfter(8);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setBackgroundColor(NAVY);
        leftCell.setPaddingTop(10);
        leftCell.setPaddingBottom(10);
        leftCell.addElement(new Phrase("FIBRE OPTIQUE", companyFont));
        leftCell.addElement(new Phrase("PLATFORM", subFont));
        header.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setBackgroundColor(NAVY);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setPaddingTop(10);
        rightCell.setPaddingBottom(10);
        rightCell.addElement(new Phrase(docType, docTypeFont));
        rightCell.addElement(new Phrase(ref, refFont));
        header.addCell(rightCell);

        doc.add(header);
    }

    // ── Section title bar ────────────────────────────────────────────────────

    public static void addSectionTitle(Document doc, String title)
            throws DocumentException {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, NAVY);
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(10);
        t.setSpacingAfter(4);
        PdfPCell c = new PdfPCell(new Phrase(title, f));
        c.setBackgroundColor(ACCENT_BLUE);
        c.setBorderColor(NAVY_LIGHT);
        c.setBorderWidth(0.5f);
        c.setPadding(6);
        t.addCell(c);
        doc.add(t);
    }

    // ── Table helpers ─────────────────────────────────────────────────────────

    public static void addTableHeader(PdfPTable table, String... headers) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, f));
            c.setBackgroundColor(NAVY);
            c.setBorderColor(NAVY_LIGHT);
            c.setBorderWidth(0.5f);
            c.setPaddingTop(7);
            c.setPaddingBottom(7);
            c.setPaddingLeft(8);
            table.addCell(c);
        }
    }

    public static void addTableRow(PdfPTable table, int rowIndex, String... cells) {
        Color bg = (rowIndex % 2 == 0) ? WHITE : GREY_ROW;
        Font  f  = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);
        for (String val : cells) {
            PdfPCell c = new PdfPCell(new Phrase(val == null ? "—" : val, f));
            c.setBackgroundColor(bg);
            c.setBorderColor(GREY_BORDER);
            c.setBorderWidth(0.4f);
            c.setPaddingTop(6);
            c.setPaddingBottom(6);
            c.setPaddingLeft(8);
            table.addCell(c);
        }
    }

    // ── Info section (key/value mini-table inside a PdfPTable cell) ───────────

    public static void addInfoSection(PdfPTable grid,
                                      String sectionTitle,
                                      String[][] rows)
            throws DocumentException {
        PdfPCell container = new PdfPCell();
        container.setBorder(Rectangle.NO_BORDER);
        container.setPaddingRight(8);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, NAVY);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA,      9, TEXT_DARK);

        PdfPTable inner = new PdfPTable(2);
        inner.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase(sectionTitle, titleFont));
        titleCell.setColspan(2);
        titleCell.setBackgroundColor(ACCENT_BLUE);
        titleCell.setBorderColor(NAVY_LIGHT);
        titleCell.setBorderWidth(0.5f);
        titleCell.setPadding(5);
        inner.addCell(titleCell);

        for (int i = 0; i < rows.length; i++) {
            Color bg = (i % 2 == 0) ? WHITE : GREY_ROW;

            PdfPCell lbl = new PdfPCell(new Phrase(rows[i][0], labelFont));
            lbl.setBackgroundColor(bg);
            lbl.setBorderColor(GREY_BORDER);
            lbl.setBorderWidth(0.4f);
            lbl.setPaddingTop(5); lbl.setPaddingBottom(5); lbl.setPaddingLeft(6);

            PdfPCell val = new PdfPCell(new Phrase(rows[i][1], valueFont));
            val.setBackgroundColor(bg);
            val.setBorderColor(GREY_BORDER);
            val.setBorderWidth(0.4f);
            val.setPaddingTop(5); val.setPaddingBottom(5); val.setPaddingLeft(6);

            inner.addCell(lbl);
            inner.addCell(val);
        }

        container.addElement(inner);
        grid.addCell(container);
    }

    // ── Footer page event ─────────────────────────────────────────────────────

    public static class FooterEvent extends PdfPageEventHelper {
        private final String text;

        public FooterEvent(String text) {
            this.text = text;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font f  = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(120, 120, 120));
            Font pf = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(120, 120, 120));

            float x = document.getPageSize().getWidth() / 2;
            float y = document.bottomMargin() - 15;

            // Separator line
            cb.setColorStroke(GREY_BORDER);
            cb.setLineWidth(0.5f);
            cb.moveTo(document.leftMargin(), y + 10);
            cb.lineTo(document.getPageSize().getWidth() - document.rightMargin(), y + 10);
            cb.stroke();

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(text, f), x, y, 0);

            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Page " + writer.getPageNumber(), pf),
                    document.getPageSize().getWidth() - document.rightMargin(), y, 0);
        }
    }
}
