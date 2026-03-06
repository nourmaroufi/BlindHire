package Service;

import Model.Candidature;
import Model.User;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports a list of Candidature objects to a styled PDF report.
 * Requires iText 5 on the classpath:
 *   <dependency>
 *     <groupId>com.itextpdf</groupId>
 *     <artifactId>itextpdf</artifactId>
 *     <version>5.5.13.3</version>
 *   </dependency>
 */
public class CandidaturePdfExportService {

    // ── Brand colours ─────────────────────────────────────────────────────────
    private static final BaseColor DARK_BG     = new BaseColor(12,  74, 110);   // #0c4a6e
    private static final BaseColor CYAN        = new BaseColor(15, 175, 221);   // #0fafdd
    private static final BaseColor LIGHT_ROW   = new BaseColor(240, 249, 255);  // #f0f9ff
    private static final BaseColor WHITE        = BaseColor.WHITE;
    private static final BaseColor TEXT_DARK    = new BaseColor(15,  23,  42);  // #0f172a
    private static final BaseColor TEXT_MUTED   = new BaseColor(100, 116, 139); // #64748b
    private static final BaseColor GREEN_BG     = new BaseColor(220, 252, 231); // accepted
    private static final BaseColor GREEN_FG     = new BaseColor(22,  101,  52);
    private static final BaseColor AMBER_BG     = new BaseColor(254, 243, 199); // pending
    private static final BaseColor AMBER_FG     = new BaseColor(146,  64,  14);
    private static final BaseColor RED_BG       = new BaseColor(254, 226, 226); // rejected
    private static final BaseColor RED_FG       = new BaseColor(153,  27,  27);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD,   WHITE);
    private static final Font FONT_SUB     = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(186, 230, 253));
    private static final Font FONT_COL_HDR = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   WHITE);
    private static final Font FONT_CELL    = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, TEXT_DARK);
    private static final Font FONT_CELL_SM = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, TEXT_MUTED);
    private static final Font FONT_SECTION = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   DARK_BG);
    private static final Font FONT_BADGE   = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   WHITE);

    public static void export(List<Candidature> candidatures,
                              CandidatureService svc,
                              String outputPath) throws Exception {

        Document document = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        writer.setPageEvent(new PageFooter());
        document.open();

        // ── Header banner ─────────────────────────────────────────────────────
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBackgroundColor(DARK_BG);
        titleCell.setPadding(20);
        titleCell.setBorder(Rectangle.NO_BORDER);

        Paragraph titlePara = new Paragraph();
        titlePara.add(new Chunk("Candidatures Report", FONT_TITLE));
        titlePara.add(Chunk.NEWLINE);
        titlePara.add(new Chunk("Generated on " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                + "   •   " + candidatures.size() + " application(s)", FONT_SUB));
        titleCell.addElement(titlePara);
        header.addCell(titleCell);
        document.add(header);
        document.add(new Paragraph(" "));

        // ── Stats summary row ─────────────────────────────────────────────────
        long pending  = candidatures.stream().filter(c -> "pending" .equalsIgnoreCase(c.getStatus())).count();
        long accepted = candidatures.stream().filter(c -> "accepted".equalsIgnoreCase(c.getStatus())).count();
        long rejected = candidatures.stream().filter(c -> "rejected".equalsIgnoreCase(c.getStatus())).count();

        PdfPTable stats = new PdfPTable(4);
        stats.setWidthPercentage(100);
        stats.setSpacingAfter(14);
        stats.setWidths(new float[]{1, 1, 1, 1});

        addStatCell(stats, String.valueOf(candidatures.size()), "Total",    DARK_BG,                       WHITE);
        addStatCell(stats, String.valueOf(pending),             "Pending",  AMBER_BG, new BaseColor(180, 83, 9));
        addStatCell(stats, String.valueOf(accepted),            "Accepted", GREEN_BG, new BaseColor(22, 101, 52));
        addStatCell(stats, String.valueOf(rejected),            "Rejected", RED_BG,   new BaseColor(153, 27, 27));
        document.add(stats);

        // ── Table header row ──────────────────────────────────────────────────
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.2f, 2.4f, 1.6f, 1.2f, 1.8f, 2.8f, 1.4f});
        table.setHeaderRows(1);

        String[] headers = {"Candidate", "Job Offer", "Applied Date", "Status",
                "Expected Salary", "Cover Letter", "Portfolio"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_COL_HDR));
            cell.setBackgroundColor(CYAN);
            cell.setPadding(8);
            cell.setBorderColor(DARK_BG);
            cell.setBorderWidth(0.5f);
            table.addCell(cell);
        }

        // ── Data rows ─────────────────────────────────────────────────────────
        boolean stripe = false;
        for (Candidature c : candidatures) {
            BaseColor rowBg = stripe ? LIGHT_ROW : WHITE;
            stripe = !stripe;

            // Candidate username
            String username = "—";
            try {
                User u = svc.getCandidateUserById(c.getCandidateId());
                if (u != null) {
                    username = u.getUsername() != null ? u.getUsername() : "Anonymous";
                }
            } catch (Exception ignored) {}

            // Job title
            String jobTitle = "—";
            try { jobTitle = svc.getJobTitleById(c.getJobOfferId()); }
            catch (Exception ignored) {}

            // Date
            String date = c.getApplicationDate() != null
                    ? c.getApplicationDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    : "—";

            // Salary
            String salary = c.getExpectedSalary() != null && c.getExpectedSalary() > 0
                    ? String.format("%.0f", c.getExpectedSalary())
                    : "—";

            // Cover letter (truncated)
            String cover = c.getCoverLetter() != null
                    ? (c.getCoverLetter().length() > 120
                    ? c.getCoverLetter().substring(0, 117) + "..."
                    : c.getCoverLetter())
                    : "—";

            // Portfolio
            String portfolio = c.getPortfolioUrl() != null && !c.getPortfolioUrl().isBlank()
                    ? c.getPortfolioUrl() : "—";

            addDataCell(table, username,  rowBg, FONT_CELL,    false);
            addDataCell(table, jobTitle,  rowBg, FONT_CELL,    false);
            addDataCell(table, date,      rowBg, FONT_CELL,    false);
            addStatusCell(table, c.getStatus(), rowBg);
            addDataCell(table, salary,    rowBg, FONT_CELL,    false);
            addDataCell(table, cover,     rowBg, FONT_CELL_SM, true);
            addDataCell(table, portfolio, rowBg, FONT_CELL_SM, false);
        }

        document.add(table);
        document.close();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void addStatCell(PdfPTable t, String value, String label,
                                    BaseColor bg, BaseColor fg) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setPadding(12);
        cell.setBorder(Rectangle.NO_BORDER);

        Font valFont  = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, fg);
        Font lblFont  = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, fg);
        Paragraph p = new Paragraph();
        p.add(new Chunk(value + "\n", valFont));
        p.add(new Chunk(label, lblFont));
        cell.addElement(p);
        t.addCell(cell);
    }

    private static void addDataCell(PdfPTable t, String text, BaseColor bg,
                                    Font font, boolean wrap) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(7);
        cell.setBorderColor(new BaseColor(226, 232, 240));
        cell.setBorderWidth(0.5f);
        cell.setNoWrap(!wrap);
        t.addCell(cell);
    }

    private static void addStatusCell(PdfPTable t, String status, BaseColor rowBg) {
        String label = status != null ? status.toLowerCase() : "pending";
        BaseColor bg, fg;
        switch (label) {
            case "accepted": bg = GREEN_BG; fg = GREEN_FG; break;
            case "rejected": bg = RED_BG;   fg = RED_FG;   break;
            default:         bg = AMBER_BG; fg = AMBER_FG; break;
        }
        Font badgeFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, fg);
        PdfPCell outer = new PdfPCell();
        outer.setBackgroundColor(rowBg);
        outer.setPadding(6);
        outer.setBorderColor(new BaseColor(226, 232, 240));
        outer.setBorderWidth(0.5f);

        PdfPTable badge = new PdfPTable(1);
        badge.setWidthPercentage(85);
        PdfPCell inner = new PdfPCell(new Phrase(
                label.substring(0,1).toUpperCase() + label.substring(1), badgeFont));
        inner.setBackgroundColor(bg);
        inner.setPaddingTop(4); inner.setPaddingBottom(4);
        inner.setPaddingLeft(6); inner.setPaddingRight(6);
        inner.setBorder(Rectangle.NO_BORDER);
        inner.setHorizontalAlignment(Element.ALIGN_CENTER);
        badge.addCell(inner);
        outer.addElement(badge);
        t.addCell(outer);
    }

    // ── Page footer event ─────────────────────────────────────────────────────
    static class PageFooter extends PdfPageEventHelper {
        private final Font footerFont = new Font(Font.FontFamily.HELVETICA, 8,
                Font.NORMAL, new BaseColor(148, 163, 184));

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase(
                    "BlindHire — Confidential   •   Page " + writer.getPageNumber(),
                    footerFont);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }
}