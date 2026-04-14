package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.Review;
import com.ahsmart.campusmarket.payloadDTOs.review.ProductReviewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Checks if a review already exists for this order by this buyer (prevents duplicates).
    boolean existsByOrder_OrderIdAndReviewer_UserId(Long orderId, Long reviewerId);

    // Returns order IDs already reviewed by this buyer for the profile page UI.
    @Query("SELECT r.order.orderId FROM Review r WHERE r.reviewer.userId = :reviewerId")
    Set<Long> findReviewedOrderIdsByReviewer(@Param("reviewerId") Long reviewerId);

    // Returns the average rating for a seller — used by SellerRatingHelper for product card stars.
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.targetSeller.userId = :sellerUserId")
    Double findAverageRatingBySellerUserId(@Param("sellerUserId") Long sellerUserId);

    // Counts total reviews received by a seller — used alongside the average for the star display.
    long countByTargetSeller_UserId(Long sellerUserId);

    // Returns all reviews from orders that contained a specific product — used for the seller sales-history modal.
    @Query("SELECT new com.ahsmart.campusmarket.payloadDTOs.review.ProductReviewDTO(" +
            "r.reviewId, r.reviewer.firstName, r.reviewer.lastName, r.rating, r.comment, r.createdAt) " +
            "FROM Review r " +
            "WHERE r.order.orderId IN (" +
            "  SELECT oi.order.orderId FROM OrderItem oi WHERE oi.product.productId = :productId" +
            ") " +
            "ORDER BY r.createdAt DESC")
    List<ProductReviewDTO> findProductReviewsByProductId(@Param("productId") Long productId);
}
