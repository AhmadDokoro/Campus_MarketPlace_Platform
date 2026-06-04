package com.ahsmart.campusmarket.service.report;

import com.ahsmart.campusmarket.payloadDTOs.admin.*;
import com.ahsmart.campusmarket.service.admin.AdminService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final AdminService adminService;

    private static final Color DEEP_PURPLE = new Color(26, 10, 62);
    private static final Color PRIMARY_PURPLE = new Color(45, 27, 105);
    private static final Color MID_PURPLE = new Color(74, 44, 138);
    private static final Color LIGHT_PURPLE = new Color(107, 78, 175);
    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color DARK_GOLD = new Color(201, 168, 0);
    private static final Color BG_LIGHT = new Color(248, 246, 252);
    private static final Color TEXT_DARK = new Color(46, 39, 64);
    private static final Color TEXT_MUTED = new Color(107, 98, 128);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color TABLE_HEADER_BG = new Color(35, 20, 80);
    private static final Color TABLE_ROW_ALT = new Color(245, 243, 250);
    private static final Color BORDER_COLOR = new Color(220, 210, 240);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");

    public byte[] generateReport() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 50);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new ReportFooter());
            doc.open();

            addHeader(doc);
            addSystemOverview(doc);
            addSellerVerification(doc);
            addCategoryBreakdown(doc);
            addWeeklyListings(doc);
            addSellerPerformance(doc);
            addFlaggedProducts(doc);
            addReportFooterNote(doc);

        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }

    private void addHeader(  Document doc) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{65, 35});
        header.setSpacingAfter(6);

        Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, DEEP_PURPLE);
        Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXT_MUTED);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPaddingBottom(8);
        Paragraph title = new Paragraph("Campus Marketplace", titleFont);
        title.setSpacingAfter(2);
        left.addElement(title);
        left.addElement(new Paragraph("System Report", new Font(Font.HELVETICA, 14, Font.BOLD, MID_PURPLE)));
        header.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setPaddingBottom(8);
        right.addElement(createAlignedParagraph("UMT Campus Marketplace",
                new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY_PURPLE), Element.ALIGN_RIGHT));
        right.addElement(createAlignedParagraph("Generated: " + LocalDateTime.now().format(DATETIME_FMT),
                subtitleFont, Element.ALIGN_RIGHT));
        right.addElement(createAlignedParagraph("Confidential — Admin Only",
                new Font(Font.HELVETICA, 8, Font.ITALIC, DANGER), Element.ALIGN_RIGHT));
        header.addCell(right);

        doc.add(header);
        addDivider(doc);
    }

    private void addSystemOverview(Document doc) throws DocumentException {
        addSectionTitle(doc, "System Overview");

        long totalUsers = adminService.getTotalUsers();
        long totalSellers = adminService.getTotalSellers();
        long verifiedSellers = adminService.getVerifiedSellers();
        long activeListings = adminService.getActiveListings();
        long totalSales = adminService.getTotalSales();
        long flaggedCount = adminService.getSuspiciousProducts().size();

        PdfPTable grid = new PdfPTable(3);
        grid.setWidthPercentage(100);
        grid.setSpacingAfter(18);

        addStatCard(grid, "Total Users", String.valueOf(totalUsers), "Registered students", PRIMARY_PURPLE);
        addStatCard(grid, "Total Sellers", String.valueOf(totalSellers), "All seller accounts", MID_PURPLE);
        addStatCard(grid, "Verified Sellers", String.valueOf(verifiedSellers), "Approved accounts", SUCCESS);
        addStatCard(grid, "Active Listings", String.valueOf(activeListings), "Products live", new Color(59, 130, 246));
        addStatCard(grid, "Flagged Listings", String.valueOf(flaggedCount), "Pending review", WARNING);
        addStatCard(grid, "Completed Sales", String.valueOf(totalSales), "Delivered transactions", DARK_GOLD);

        doc.add(grid);
    }

    private void addStatCard(PdfPTable grid, String label, String value, String desc, Color accent) {
        PdfPCell card = new PdfPCell();
        card.setBorder(Rectangle.BOX);
        card.setBorderColor(BORDER_COLOR);
        card.setBorderWidth(0.8f);
        card.setPadding(12);
        card.setPaddingBottom(14);
        card.setBackgroundColor(Color.WHITE);

        Font labelFont = new Font(Font.HELVETICA, 8, Font.BOLD, TEXT_MUTED);
        Font valueFont = new Font(Font.HELVETICA, 22, Font.BOLD, accent);
        Font descFont = new Font(Font.HELVETICA, 7, Font.NORMAL, TEXT_MUTED);

        Paragraph lbl = new Paragraph(label.toUpperCase(), labelFont);
        lbl.setSpacingAfter(4);
        card.addElement(lbl);

        Paragraph val = new Paragraph(value, valueFont);
        val.setSpacingAfter(2);
        card.addElement(val);

        card.addElement(new Paragraph(desc, descFont));
        grid.addCell(card);
    }

    private void addSellerVerification(Document doc) throws DocumentException {
        addSectionTitle(doc, "Seller Verification Summary");

        Map<String, Long> stats = adminService.getVerificationStatusStats();
        long approved = stats.getOrDefault("APPROVED", 0L);
        long pending = stats.getOrDefault("PENDING", 0L);
        long rejected = stats.getOrDefault("REJECTED", 0L);
        long total = approved + pending + rejected;

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(70);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(18);
        table.setWidths(new float[]{40, 25, 35});

        addTableHeader(table, "Status", "Count", "Percentage");

        addVerificationRow(table, "Approved", approved, total, SUCCESS, 0);
        addVerificationRow(table, "Pending", pending, total, WARNING, 1);
        addVerificationRow(table, "Rejected", rejected, total, DANGER, 2);

        doc.add(table);
    }

    private void addVerificationRow(PdfPTable table, String status, long count, long total, Color color, int row) {
        Color bg = row % 2 == 1 ? TABLE_ROW_ALT : Color.WHITE;
        String pct = total > 0 ? String.format("%.1f%%", (count * 100.0) / total) : "0%";

        addCell(table, status, new Font(Font.HELVETICA, 9, Font.BOLD, color), bg, Element.ALIGN_LEFT);
        addCell(table, String.valueOf(count), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_DARK), bg, Element.ALIGN_CENTER);
        addCell(table, pct, new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_DARK), bg, Element.ALIGN_CENTER);
    }

    private String buildBar(int filled) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filled; i++) sb.append('█');
        for (int i = filled; i < 20; i++) sb.append('░');
        return sb.toString();
    }

    private void addCategoryBreakdown(Document doc) throws DocumentException {
        addSectionTitle(doc, "Category Breakdown");

        List<CategoryStatsDTO> categories = adminService.getTopCategories();

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(70);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(18);
        table.setWidths(new float[]{10, 55, 35});

        addTableHeader(table, "#", "Category", "Listings");

        int rank = 1;
        for (CategoryStatsDTO cat : categories) {
            Color bg = rank % 2 == 0 ? TABLE_ROW_ALT : Color.WHITE;
            addCell(table, String.valueOf(rank), new Font(Font.HELVETICA, 9, Font.BOLD, MID_PURPLE), bg, Element.ALIGN_CENTER);
            addCell(table, cat.getCategoryName(), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_DARK), bg, Element.ALIGN_LEFT);
            addCell(table, String.valueOf(cat.getProductCount()), new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY_PURPLE), bg, Element.ALIGN_CENTER);
            rank++;
        }

        doc.add(table);
    }

    private void addWeeklyListings(Document doc) throws DocumentException {
        addSectionTitle(doc, "Weekly Listings (Last 8 Weeks)");

        List<WeeklyListingDTO> weekly = adminService.getWeeklyListings();

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(70);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(18);
        table.setWidths(new float[]{40, 25, 35});

        addTableHeader(table, "Week", "New Listings", "Activity");

        int maxCount = weekly.stream().mapToInt(w -> (int) w.getCount()).max().orElse(1);
        int row = 0;
        for (WeeklyListingDTO w : weekly) {
            Color bg = row % 2 == 1 ? TABLE_ROW_ALT : Color.WHITE;
            addCell(table, w.getWeekLabel(), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_DARK), bg, Element.ALIGN_LEFT);
            addCell(table, String.valueOf(w.getCount()), new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY_PURPLE), bg, Element.ALIGN_CENTER);
            String bar = buildBar(maxCount > 0 ? (int) ((w.getCount() * 20) / maxCount) : 0);
            addCell(table, bar, new Font(Font.HELVETICA, 9, Font.NORMAL, LIGHT_PURPLE), bg, Element.ALIGN_LEFT);
            row++;
        }

        doc.add(table);
    }

    private void addSellerPerformance(Document doc) throws DocumentException {
        addSectionTitle(doc, "Seller Performance");

        List<SellerStatDTO> sellers = adminService.getSellerStats();

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingAfter(18);
        table.setWidths(new float[]{8, 30, 22, 18, 22});

        addTableHeader(table, "#", "Seller", "Joined", "Listings", "Status");

        int row = 0;
        for (SellerStatDTO s : sellers) {
            Color bg = row % 2 == 1 ? TABLE_ROW_ALT : Color.WHITE;

            addCell(table, String.valueOf(row + 1), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED), bg, Element.ALIGN_CENTER);
            addCell(table, s.getSellerName(), new Font(Font.HELVETICA, 9, Font.BOLD, TEXT_DARK), bg, Element.ALIGN_LEFT);

            String date = s.getSubmittedAt() != null ? s.getSubmittedAt().format(DATE_FMT) : "N/A";
            addCell(table, date, new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED), bg, Element.ALIGN_LEFT);

            addCell(table, String.valueOf(s.getListingCount()), new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY_PURPLE), bg, Element.ALIGN_CENTER);

            Color statusColor = switch (s.getStatusName()) {
                case "APPROVED" -> SUCCESS;
                case "PENDING" -> WARNING;
                case "REJECTED" -> DANGER;
                default -> TEXT_MUTED;
            };
            addCell(table, s.getStatusName(), new Font(Font.HELVETICA, 9, Font.BOLD, statusColor), bg, Element.ALIGN_CENTER);
            row++;
        }

        doc.add(table);
    }

    private void addFlaggedProducts(Document doc) throws DocumentException {
        List<FlaggedProductDTO> flagged = adminService.getSuspiciousProducts();
        addSectionTitle(doc, "Flagged Products (" + flagged.size() + ")");

        if (flagged.isEmpty()) {
            Font emptyFont = new Font(Font.HELVETICA, 10, Font.ITALIC, TEXT_MUTED);
            Paragraph empty = new Paragraph("No suspicious products at this time.", emptyFont);
            empty.setSpacingAfter(18);
            doc.add(empty);
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingAfter(18);
        table.setWidths(new float[]{8, 28, 18, 15, 15, 16});

        addTableHeader(table, "#", "Product", "Category", "Price (RM)", "Seller", "Listed");

        int row = 0;
        for (FlaggedProductDTO p : flagged) {
            Color bg = row % 2 == 1 ? TABLE_ROW_ALT : Color.WHITE;
            addCell(table, String.valueOf(row + 1), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED), bg, Element.ALIGN_CENTER);
            addCell(table, truncate(p.getTitle(), 35), new Font(Font.HELVETICA, 9, Font.BOLD, DANGER), bg, Element.ALIGN_LEFT);
            addCell(table, p.getCategoryName(), new Font(Font.HELVETICA, 8, Font.NORMAL, TEXT_MUTED), bg, Element.ALIGN_LEFT);
            addCell(table, p.getPrice().toPlainString(), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_DARK), bg, Element.ALIGN_RIGHT);
            addCell(table, p.getSellerName(), new Font(Font.HELVETICA, 8, Font.NORMAL, TEXT_DARK), bg, Element.ALIGN_LEFT);
            String date = p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : "N/A";
            addCell(table, date, new Font(Font.HELVETICA, 8, Font.NORMAL, TEXT_MUTED), bg, Element.ALIGN_LEFT);
            row++;
        }

        doc.add(table);
    }

    private void addReportFooterNote(Document doc) throws DocumentException {
        addDivider(doc);

        Font noteFont = new Font(Font.HELVETICA, 8, Font.ITALIC, TEXT_MUTED);
        Paragraph note = new Paragraph(
                "This report was auto-generated by UMT Campus Marketplace admin system. " +
                "Data reflects the state of the system at the time of generation. " +
                "For real-time data, refer to the admin dashboard.", noteFont);
        note.setSpacingBefore(6);
        note.setAlignment(Element.ALIGN_CENTER);
        doc.add(note);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private void addSectionTitle(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, new Font(Font.HELVETICA, 14, Font.BOLD, DEEP_PURPLE));
        p.setSpacingBefore(14);
        p.setSpacingAfter(10);
        doc.add(p);
    }

    private void addDivider(Document doc) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        line.setSpacingBefore(4);
        line.setSpacingAfter(4);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(1.5f);
        cell.setFixedHeight(1);
        line.addCell(cell);
        doc.add(line);
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font hFont = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h.toUpperCase(), hFont));
            cell.setBackgroundColor(TABLE_HEADER_BG);
            cell.setPadding(8);
            cell.setBorderColor(TABLE_HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(7);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(0.5f);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private Paragraph createAlignedParagraph(String text, Font font, int align) {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(align);
        return p;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private static class ReportFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font footFont = new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(107, 98, 128));

            Phrase left = new Phrase("UMT Campus Marketplace - Confidential", footFont);
            Phrase right = new Phrase("Page " + writer.getPageNumber(), footFont);

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, left,
                    document.left(), document.bottom() - 20, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, right,
                    document.right(), document.bottom() - 20, 0);
        }
    }
}
