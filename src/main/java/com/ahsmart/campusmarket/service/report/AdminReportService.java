package com.ahsmart.campusmarket.service.report;

import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.ProductImage;
import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.payloadDTOs.admin.CategoryStatsDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.FlaggedProductDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.SellerStatDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.WeeklyListingDTO;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final UsersRepository usersRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    private static final Color DEEP_PURPLE = new Color(26, 10, 62);
    private static final Color PRIMARY_PURPLE = new Color(45, 27, 105);
    private static final Color MID_PURPLE = new Color(74, 44, 138);
    private static final Color LIGHT_PURPLE = new Color(107, 78, 175);
    private static final Color DARK_GOLD = new Color(201, 168, 0);
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
    private static final DateTimeFormatter DAY_LABEL_FMT = DateTimeFormatter.ofPattern("MMM d");

    public List<Integer> getSelectableYears() {
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int year = currentYear; year >= currentYear - 4; year--) {
            years.add(year);
        }
        return years;
    }

    public AdminReportData buildReport(Integer year, Integer month) {
        AdminReportPeriod period = AdminReportPeriod.of(year, month);
        LocalDateTime generatedAt = LocalDateTime.now();

        long totalUsers = usersRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(period.start(), period.end());
        long totalSellers = sellerRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(period.start(), period.end());
        long verifiedSellers = sellerRepository.countByStatusAndSubmittedAtGreaterThanEqualAndSubmittedAtLessThan(
                SellerStatus.APPROVED, period.start(), period.end());
        long activeListings = productRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(period.start(), period.end());
        long flaggedListings = productRepository.countByFlaggedStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                FlaggedStatus.SUSPICIOUS, period.start(), period.end());
        long totalSales = orderItemRepository.countByDeliveryStatusAndOrder_CreatedAtGreaterThanEqualAndOrder_CreatedAtLessThan(
                DeliveryStatus.RECEIVED, period.start(), period.end());

        Map<String, Long> verificationStats = new LinkedHashMap<>();
        verificationStats.put("PENDING", sellerRepository.countByStatusAndSubmittedAtGreaterThanEqualAndSubmittedAtLessThan(
                SellerStatus.PENDING, period.start(), period.end()));
        verificationStats.put("APPROVED", verifiedSellers);
        verificationStats.put("REJECTED", sellerRepository.countByStatusAndSubmittedAtGreaterThanEqualAndSubmittedAtLessThan(
                SellerStatus.REJECTED, period.start(), period.end()));

        List<CategoryStatsDTO> topCategories = mapCategoryStats(
                productRepository.countProductsPerCategoryWithinPeriod(period.start(), period.end()));
        List<WeeklyListingDTO> listingActivity = buildListingActivity(period);
        String listingActivityLabel = period.isMonthly() ? "Daily Listings" : "Monthly Listings";
        List<SellerStatDTO> sellerStats = mapSellerStats(
                sellerRepository.findSellerStatsWithinPeriod(period.start(), period.end()));
        List<FlaggedProductDTO> flaggedProducts = mapFlaggedProducts(
                productRepository.findByFlaggedStatusWithDetailsWithinPeriod(
                        FlaggedStatus.SUSPICIOUS, period.start(), period.end()));

        return new AdminReportData(
                period,
                generatedAt,
                totalUsers,
                totalSellers,
                verifiedSellers,
                activeListings,
                flaggedListings,
                totalSales,
                verificationStats,
                topCategories,
                listingActivity,
                listingActivityLabel,
                sellerStats,
                flaggedProducts
        );
    }

    public byte[] generateReportPdf(AdminReportData reportData) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 50);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new ReportFooter());
            doc.open();

            addHeader(doc, reportData);
            addSystemOverview(doc, reportData);
            addSellerVerification(doc, reportData);
            addCategoryBreakdown(doc, reportData);
            addListingActivity(doc, reportData);
            addSellerPerformance(doc, reportData);
            addFlaggedProducts(doc, reportData);
            addReportFooterNote(doc, reportData);
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }

    public String buildFilename(AdminReportPeriod period) {
        return "CampusMarketplace_Report_" + period.fileLabel() + ".pdf";
    }

    private List<CategoryStatsDTO> mapCategoryStats(List<Object[]> rows) {
        List<CategoryStatsDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            String name = row[0] == null ? "Uncategorized" : row[0].toString();
            long count = ((Number) row[1]).longValue();
            result.add(new CategoryStatsDTO(name, count));
        }
        return result;
    }

    private List<SellerStatDTO> mapSellerStats(List<Object[]> rows) {
        List<SellerStatDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long sellerId = ((Number) row[0]).longValue();
            String firstName = row[1] == null ? "" : row[1].toString();
            String lastName = row[2] == null ? "" : row[2].toString();
            String sellerName = (firstName + " " + lastName).trim();
            if (sellerName.isEmpty()) {
                sellerName = "Unknown";
            }

            LocalDateTime submittedAt = null;
            if (row[3] instanceof Timestamp timestamp) {
                submittedAt = timestamp.toLocalDateTime();
            } else if (row[3] instanceof LocalDateTime localDateTime) {
                submittedAt = localDateTime;
            }

            String statusName = row[4] == null ? "UNKNOWN" : row[4].toString();
            long listingCount = row[5] == null ? 0L : ((Number) row[5]).longValue();
            result.add(new SellerStatDTO(sellerId, sellerName, submittedAt, listingCount, statusName));
        }
        return result;
    }

    private List<FlaggedProductDTO> mapFlaggedProducts(List<Product> products) {
        List<FlaggedProductDTO> result = new ArrayList<>();
        for (Product product : products) {
            String primaryImageUrl = product.getImages().stream()
                    .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(null);
            String sellerName = product.getSeller() != null && product.getSeller().getUser() != null
                    ? (product.getSeller().getUser().getFirstName() + " " + product.getSeller().getUser().getLastName()).trim()
                    : "Unknown";
            String categoryName = product.getCategory() != null
                    ? product.getCategory().getCategoryName()
                    : "Uncategorized";
            String conditionName = product.getCondition() != null
                    ? product.getCondition().name()
                    : "-";
            result.add(new FlaggedProductDTO(
                    product.getProductId(),
                    product.getTitle(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getQuantity(),
                    conditionName,
                    categoryName,
                    sellerName,
                    primaryImageUrl,
                    product.getCreatedAt(),
                    null,           // AI confidence not surfaced in periodic reports
                    List.of()       // AI reasons not surfaced in periodic reports
            ));
        }
        return result;
    }

    private List<WeeklyListingDTO> buildListingActivity(AdminReportPeriod period) {
        List<LocalDateTime> createdAtValues = productRepository.findCreatedAtWithinPeriod(period.start(), period.end());
        Map<String, Long> counts = new LinkedHashMap<>();

        if (period.isMonthly()) {
            LocalDate date = period.start().toLocalDate();
            LocalDate endDate = period.end().toLocalDate().minusDays(1);
            for (LocalDate current = date; !current.isAfter(endDate); current = current.plusDays(1)) {
                counts.put(current.format(DAY_LABEL_FMT), 0L);
            }
            for (LocalDateTime createdAt : createdAtValues) {
                String key = createdAt.toLocalDate().format(DAY_LABEL_FMT);
                counts.put(key, counts.getOrDefault(key, 0L) + 1);
            }
        } else {
            for (int month = 1; month <= 12; month++) {
                String label = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                counts.put(label, 0L);
            }
            for (LocalDateTime createdAt : createdAtValues) {
                String key = createdAt.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                counts.put(key, counts.getOrDefault(key, 0L) + 1);
            }
        }

        List<WeeklyListingDTO> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            result.add(new WeeklyListingDTO(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private void addHeader(Document doc, AdminReportData reportData) throws DocumentException {
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
        left.addElement(new Paragraph("Report for " + reportData.period().label(),
                new Font(Font.HELVETICA, 14, Font.BOLD, MID_PURPLE)));
        header.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setPaddingBottom(8);
        right.addElement(createAlignedParagraph("UMT Campus Marketplace",
                new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY_PURPLE), Element.ALIGN_RIGHT));
        right.addElement(createAlignedParagraph("Generated: " + reportData.generatedAt().format(DATETIME_FMT),
                subtitleFont, Element.ALIGN_RIGHT));
        right.addElement(createAlignedParagraph("Confidential - Admin Only",
                new Font(Font.HELVETICA, 8, Font.ITALIC, DANGER), Element.ALIGN_RIGHT));
        header.addCell(right);

        doc.add(header);
        addDivider(doc);
    }

    private void addSystemOverview(Document doc, AdminReportData reportData) throws DocumentException {
        addSectionTitle(doc, "Period Overview");

        PdfPTable grid = new PdfPTable(3);
        grid.setWidthPercentage(100);
        grid.setSpacingAfter(18);

        addStatCard(grid, "Users Registered", String.valueOf(reportData.totalUsers()), "New accounts in period", PRIMARY_PURPLE);
        addStatCard(grid, "Seller Requests", String.valueOf(reportData.totalSellers()), "Applications submitted", MID_PURPLE);
        addStatCard(grid, "Approved Requests", String.valueOf(reportData.verifiedSellers()), "Approved submissions", SUCCESS);
        addStatCard(grid, "Listings Added", String.valueOf(reportData.activeListings()), "Products created", new Color(59, 130, 246));
        addStatCard(grid, "Flagged Listings", String.valueOf(reportData.flaggedListings()), "Suspicious products", WARNING);
        addStatCard(grid, "Completed Sales", String.valueOf(reportData.totalSales()), "Received order items", DARK_GOLD);

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

    private void addSellerVerification(Document doc, AdminReportData reportData) throws DocumentException {
        addSectionTitle(doc, "Seller Request Status");

        Map<String, Long> stats = reportData.verificationStats();
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

    private void addCategoryBreakdown(Document doc, AdminReportData reportData) throws DocumentException {
        addSectionTitle(doc, "Listings by Category");

        List<CategoryStatsDTO> categories = reportData.topCategories();
        if (categories.isEmpty()) {
            addEmptyState(doc, "No category activity found for the selected period.");
            return;
        }

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(70);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(18);
        table.setWidths(new float[]{10, 55, 35});

        addTableHeader(table, "#", "Category", "Listings");

        int rank = 1;
        for (CategoryStatsDTO category : categories) {
            Color bg = rank % 2 == 0 ? TABLE_ROW_ALT : Color.WHITE;
            addCell(table, String.valueOf(rank), new Font(Font.HELVETICA, 9, Font.BOLD, MID_PURPLE), bg, Element.ALIGN_CENTER);
            addCell(table, category.getCategoryName(), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_DARK), bg, Element.ALIGN_LEFT);
            addCell(table, String.valueOf(category.getProductCount()), new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY_PURPLE), bg, Element.ALIGN_CENTER);
            rank++;
        }

        doc.add(table);
    }

    private void addListingActivity(Document doc, AdminReportData reportData) throws DocumentException {
        addSectionTitle(doc, reportData.listingActivityLabel());

        List<WeeklyListingDTO> listingActivity = reportData.listingActivity();
        if (listingActivity.isEmpty()) {
            addEmptyState(doc, "No listings were created in the selected period.");
            return;
        }

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(70);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(18);
        table.setWidths(new float[]{40, 25, 35});

        addTableHeader(table, reportData.period().isMonthly() ? "Day" : "Month", "New Listings", "Activity");

        int maxCount = listingActivity.stream().mapToInt(item -> (int) item.getCount()).max().orElse(1);
        int row = 0;
        for (WeeklyListingDTO item : listingActivity) {
            Color bg = row % 2 == 1 ? TABLE_ROW_ALT : Color.WHITE;
            addCell(table, item.getWeekLabel(), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_DARK), bg, Element.ALIGN_LEFT);
            addCell(table, String.valueOf(item.getCount()), new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY_PURPLE), bg, Element.ALIGN_CENTER);
            String bar = buildBar(maxCount > 0 ? (int) ((item.getCount() * 20) / maxCount) : 0);
            addCell(table, bar, new Font(Font.HELVETICA, 9, Font.NORMAL, LIGHT_PURPLE), bg, Element.ALIGN_LEFT);
            row++;
        }

        doc.add(table);
    }

    private String buildBar(int filled) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filled; i++) {
            sb.append('=');
        }
        for (int i = filled; i < 20; i++) {
            sb.append('.');
        }
        return sb.toString();
    }

    private void addSellerPerformance(Document doc, AdminReportData reportData) throws DocumentException {
        addSectionTitle(doc, "Seller Applications");

        List<SellerStatDTO> sellers = reportData.sellerStats();
        if (sellers.isEmpty()) {
            addEmptyState(doc, "No seller applications were submitted in the selected period.");
            return;
        }

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingAfter(18);
        table.setWidths(new float[]{8, 30, 22, 18, 22});

        addTableHeader(table, "#", "Seller", "Submitted", "Listings", "Status");

        int row = 0;
        for (SellerStatDTO seller : sellers) {
            Color bg = row % 2 == 1 ? TABLE_ROW_ALT : Color.WHITE;

            addCell(table, String.valueOf(row + 1), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED), bg, Element.ALIGN_CENTER);
            addCell(table, seller.getSellerName(), new Font(Font.HELVETICA, 9, Font.BOLD, TEXT_DARK), bg, Element.ALIGN_LEFT);

            String date = seller.getSubmittedAt() != null ? seller.getSubmittedAt().format(DATE_FMT) : "N/A";
            addCell(table, date, new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED), bg, Element.ALIGN_LEFT);
            addCell(table, String.valueOf(seller.getListingCount()), new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY_PURPLE), bg, Element.ALIGN_CENTER);

            Color statusColor = switch (seller.getStatusName()) {
                case "APPROVED" -> SUCCESS;
                case "PENDING" -> WARNING;
                case "REJECTED" -> DANGER;
                default -> TEXT_MUTED;
            };
            addCell(table, seller.getStatusName(), new Font(Font.HELVETICA, 9, Font.BOLD, statusColor), bg, Element.ALIGN_CENTER);
            row++;
        }

        doc.add(table);
    }

    private void addFlaggedProducts(Document doc, AdminReportData reportData) throws DocumentException {
        List<FlaggedProductDTO> flaggedProducts = reportData.flaggedProducts();
        addSectionTitle(doc, "Flagged Products");

        if (flaggedProducts.isEmpty()) {
            addEmptyState(doc, "No flagged products were found for the selected period.");
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingAfter(18);
        table.setWidths(new float[]{8, 28, 18, 15, 15, 16});

        addTableHeader(table, "#", "Product", "Category", "Price (RM)", "Seller", "Listed");

        int row = 0;
        for (FlaggedProductDTO product : flaggedProducts) {
            Color bg = row % 2 == 1 ? TABLE_ROW_ALT : Color.WHITE;
            addCell(table, String.valueOf(row + 1), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_MUTED), bg, Element.ALIGN_CENTER);
            addCell(table, truncate(product.getTitle(), 35), new Font(Font.HELVETICA, 9, Font.BOLD, DANGER), bg, Element.ALIGN_LEFT);
            addCell(table, product.getCategoryName(), new Font(Font.HELVETICA, 8, Font.NORMAL, TEXT_MUTED), bg, Element.ALIGN_LEFT);
            addCell(table, product.getPrice().toPlainString(), new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_DARK), bg, Element.ALIGN_RIGHT);
            addCell(table, product.getSellerName(), new Font(Font.HELVETICA, 8, Font.NORMAL, TEXT_DARK), bg, Element.ALIGN_LEFT);
            String date = product.getCreatedAt() != null ? product.getCreatedAt().format(DATE_FMT) : "N/A";
            addCell(table, date, new Font(Font.HELVETICA, 8, Font.NORMAL, TEXT_MUTED), bg, Element.ALIGN_LEFT);
            row++;
        }

        doc.add(table);
    }

    private void addReportFooterNote(Document doc, AdminReportData reportData) throws DocumentException {
        addDivider(doc);

        Font noteFont = new Font(Font.HELVETICA, 8, Font.ITALIC, TEXT_MUTED);
        Paragraph note = new Paragraph(
                "This report was generated for " + reportData.period().label() + ". " +
                        "Data shown here is filtered to the selected period and may differ from the live dashboard snapshot.",
                noteFont);
        note.setSpacingBefore(6);
        note.setAlignment(Element.ALIGN_CENTER);
        doc.add(note);
    }

    private void addEmptyState(Document doc, String message) throws DocumentException {
        Font emptyFont = new Font(Font.HELVETICA, 10, Font.ITALIC, TEXT_MUTED);
        Paragraph empty = new Paragraph(message, emptyFont);
        empty.setSpacingAfter(18);
        doc.add(empty);
    }

    private void addSectionTitle(Document doc, String title) throws DocumentException {
        Paragraph paragraph = new Paragraph(title, new Font(Font.HELVETICA, 14, Font.BOLD, DEEP_PURPLE));
        paragraph.setSpacingBefore(14);
        paragraph.setSpacingAfter(10);
        doc.add(paragraph);
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
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header.toUpperCase(), hFont));
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
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(align);
        return paragraph;
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() > max ? text.substring(0, max - 3) + "..." : text;
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
