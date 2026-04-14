package com.ahsmart.campusmarket.service.admin;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.payloadDTOs.admin.CategoryStatsDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.FlaggedProductDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.SellerStatDTO;
import com.ahsmart.campusmarket.payloadDTOs.admin.WeeklyListingDTO;

import java.util.List;
import java.util.Map;

public interface AdminService {

    // Get all sellers with PENDING status for admin review
    List<Seller> getPendingSellers();

    // Get single seller by id for review
    Seller getSellerForReview(Long sellerId);

    // Review a seller: approve or reject and set reviewer id
    Seller reviewSeller(Long sellerId, SellerStatus status, Long reviewerId);

    // Reject seller with a required reason (<= 500 chars)
    Seller rejectSeller(Long sellerId, Long reviewerId, String rejectionReason);

    // ── Analytics ─────────────────────────────────────────────────────

    long getTotalUsers();

    long getTotalSellers();

    long getVerifiedSellers();

    long getActiveListings();

    // Count of order_items with delivery_status = RECEIVED (completed transactions).
    long getTotalSales();

    // Products created per week for the last 8 weeks.
    List<WeeklyListingDTO> getWeeklyListings();

    // Seller counts grouped by verification status: keys PENDING, APPROVED, REJECTED.
    Map<String, Long> getVerificationStatusStats();

    // Products per category sorted descending by count.
    List<CategoryStatsDTO> getTopCategories();

    // Seller name, join date, listing count, and status for the manage-sellers table.
    List<SellerStatDTO> getSellerStats();

    // ── Flagged Products ───────────────────────────────────────────────

    // Returns all products with SUSPICIOUS flagged status for admin review.
    List<FlaggedProductDTO> getSuspiciousProducts();

    // Marks a flagged product as VERIFIED (approved by admin).
    void approveProduct(Long productId);

    // Deletes a product and its Cloudinary images — admin bypass (no seller ownership check).
    void adminDeleteProduct(Long productId);
}
