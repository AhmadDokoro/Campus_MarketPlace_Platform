package com.ahsmart.campusmarket.service.admin;

import com.ahsmart.campusmarket.helper.EmailHelper;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.ProductImage;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.payloadDTOs.admin.CategoryStatsDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.FlaggedProductDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.SellerStatDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.WeeklyListingDTO;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.ProductImageRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.service.product.FileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    private final SellerRepository sellerRepository;
    private final UsersRepository usersRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final EmailHelper emailHelper;
    private final ProductImageRepository productImageRepository;
    private final FileService fileService;

    public AdminServiceImpl(SellerRepository sellerRepository,
                            UsersRepository usersRepository,
                            ProductRepository productRepository,
                            OrderItemRepository orderItemRepository,
                            EmailHelper emailHelper,
                            ProductImageRepository productImageRepository,
                            FileService fileService) {
        this.sellerRepository = sellerRepository;
        this.usersRepository = usersRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.emailHelper = emailHelper;
        this.productImageRepository = productImageRepository;
        this.fileService = fileService;
    }

    @Override
    public List<Seller> getPendingSellers() {
        // return all sellers where status == PENDING
        return sellerRepository.findByStatus(SellerStatus.PENDING);
    }

    @Override
    public Seller getSellerForReview(Long sellerId) {
        // fetch seller by id with user + mentor loaded
        Optional<Seller> optional = sellerRepository.findByIdWithUserAndMentor(sellerId);
        return optional.orElseThrow(() -> new IllegalArgumentException("Seller not found"));
    }

    @Override
    @Transactional
    public Seller reviewSeller(Long sellerId, SellerStatus status, Long reviewerId) {
        Seller seller = getSellerForReview(sellerId);
        seller.setStatus(status);

        if (reviewerId != null) {
            Optional<Users> reviewerOpt = usersRepository.findById(reviewerId);
            reviewerOpt.ifPresent(seller::setReviewer);
        }

        // Keep rejectionReason ONLY for rejected sellers.
        if (status == SellerStatus.APPROVED) {
            seller.setRejectionReason(null);

            // IMPORTANT: when seller gets approved, promote the user role to SELLER.
            Users u = seller.getUser();
            if (u != null && u.getRole() != Role.SELLER) {
                u.setRole(Role.SELLER);
                usersRepository.save(u);
            }

            emailHelper.sendEmail(
                seller.getUser().getEmail(),
                "Seller Verification Approved – Campus Marketplace Platform",
                "Dear " + seller.getUser().getFirstName() + ",\n\n" +
                "Congratulations! Your seller verification request has been approved.\n" +
                "You can now list products and start selling on the Campus Marketplace Platform.\n\n" +
                "Best regards,\nCampus Marketplace Platform"
            );
        }

        return sellerRepository.save(seller);
    }

    // ── Analytics implementations ──────────────────────────────────────

    @Override
    public long getTotalUsers() {
        return usersRepository.count();
    }

    @Override
    public long getTotalSellers() {
        return sellerRepository.count();
    }

    @Override
    public long getVerifiedSellers() {
        return sellerRepository.countByStatus(SellerStatus.APPROVED);
    }

    @Override
    public long getActiveListings() {
        return productRepository.count();
    }

    @Override
    public long getTotalSales() {
        return orderItemRepository.countByDeliveryStatus(DeliveryStatus.RECEIVED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyListingDTO> getWeeklyListings() {
        List<Object[]> rows = productRepository.countProductsPerWeekLast8();
        Map<Integer, Long> countByYw = new HashMap<>();
        for (Object[] row : rows) {
            int yw = ((Number) row[0]).intValue();
            long cnt = ((Number) row[1]).longValue();
            countByYw.put(yw, cnt);
        }

        List<WeeklyListingDTO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate currentMonday = today.with(DayOfWeek.MONDAY);
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

        for (int i = 7; i >= 0; i--) {
            LocalDate weekMonday = currentMonday.minusWeeks(i);
            int year = weekMonday.getYear();
            int isoWeek = weekMonday.get(WeekFields.ISO.weekOfWeekBasedYear());
            int yw = year * 100 + isoWeek;
            String label = weekMonday.format(labelFmt);
            long count = countByYw.getOrDefault(yw, 0L);
            result.add(new WeeklyListingDTO(label, count));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getVerificationStatusStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("PENDING",  sellerRepository.countByStatus(SellerStatus.PENDING));
        stats.put("APPROVED", sellerRepository.countByStatus(SellerStatus.APPROVED));
        stats.put("REJECTED", sellerRepository.countByStatus(SellerStatus.REJECTED));
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryStatsDTO> getTopCategories() {
        List<Object[]> rows = productRepository.countProductsPerCategory();
        List<CategoryStatsDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            String name = (String) row[0];
            long count = ((Number) row[1]).longValue();
            result.add(new CategoryStatsDTO(name, count));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerStatDTO> getSellerStats() {
        List<Object[]> rows = sellerRepository.findSellerStats();
        List<SellerStatDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long sellerId    = ((Number) row[0]).longValue();
            String firstName = row[1] == null ? "" : (String) row[1];
            String lastName  = row[2] == null ? "" : (String) row[2];
            String name = (firstName + " " + lastName).trim();
            if (name.isEmpty()) name = "Unknown";

            LocalDateTime submittedAt = null;
            if (row[3] instanceof Timestamp ts) {
                submittedAt = ts.toLocalDateTime();
            }

            String statusName = row[4] == null ? "UNKNOWN" : (String) row[4];
            long listingCount = ((Number) row[5]).longValue();
            result.add(new SellerStatDTO(sellerId, name, submittedAt, listingCount, statusName));
        }
        return result;
    }

    // ── Flagged Products ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<FlaggedProductDTO> getSuspiciousProducts() {
        List<Product> products = productRepository.findByFlaggedStatusWithDetails(FlaggedStatus.SUSPICIOUS);
        List<FlaggedProductDTO> result = new ArrayList<>();
        for (Product p : products) {
            String primaryImageUrl = p.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(null);
            String sellerName = p.getSeller() != null && p.getSeller().getUser() != null
                    ? (p.getSeller().getUser().getFirstName() + " " + p.getSeller().getUser().getLastName()).trim()
                    : "Unknown";
            String categoryName = p.getCategory() != null ? p.getCategory().getCategoryName() : "—";
            String conditionName = p.getCondition() != null ? p.getCondition().name() : "—";
            result.add(new FlaggedProductDTO(
                    p.getProductId(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getPrice(),
                    p.getQuantity(),
                    conditionName,
                    categoryName,
                    sellerName,
                    primaryImageUrl,
                    p.getCreatedAt()
            ));
        }
        return result;
    }

    @Override
    @Transactional
    public void approveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        product.setFlaggedStatus(FlaggedStatus.VERIFIED);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void adminDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        productImageRepository.findByProduct_ProductId(productId)
                .forEach(image -> fileService.deleteImageByUrl(image.getImageUrl()));
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public Seller rejectSeller(Long sellerId, Long reviewerId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        String trimmed = rejectionReason.trim();
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException("Rejection reason must be 500 characters or less");
        }

        Seller seller = getSellerForReview(sellerId);
        seller.setStatus(SellerStatus.REJECTED);
        seller.setRejectionReason(trimmed);

        if (reviewerId != null) {
            usersRepository.findById(reviewerId).ifPresent(seller::setReviewer);
        }

        emailHelper.sendEmail(
            seller.getUser().getEmail(),
            "Seller Verification Rejected – Campus Marketplace Platform",
            "Dear " + seller.getUser().getFirstName() + ",\n\n" +
            "Your seller verification request has been rejected for the following reason:\n" +
            rejectionReason + "\n\n" +
            "You may correct the issue and reapply.\n\n" +
            "Best regards,\nCampus Marketplace Platform"
        );

        return sellerRepository.save(seller);
    }
}
