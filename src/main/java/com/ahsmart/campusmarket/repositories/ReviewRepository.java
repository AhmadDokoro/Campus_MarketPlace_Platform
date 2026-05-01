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

    // Returns reviewed order item ids for the buyer profile. Legacy order-level reviews are mapped back by order + seller.
    @Query("SELECT DISTINCT oi.orderItemId FROM Review r " +
            "JOIN r.order o " +
            "JOIN o.orderItems oi " +
            "JOIN oi.seller s " +
            "JOIN s.user sellerUser " +
            "WHERE r.reviewer.userId = :reviewerId " +
            "AND (r.orderItem = oi OR (r.orderItem IS NULL AND sellerUser.userId = r.targetSeller.userId))")
    Set<Long> findReviewedOrderItemIdsByReviewer(@Param("reviewerId") Long reviewerId);

    // Returns the average rating for a seller — used by SellerRatingHelper for product card stars.
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.targetSeller.userId = :sellerUserId")
    Double findAverageRatingBySellerUserId(@Param("sellerUserId") Long sellerUserId);

    // Counts total reviews received by a seller — used alongside the average for the star display.
    long countByTargetSeller_UserId(Long sellerUserId);

    // Returns the average rating for a specific product using item-level reviews and legacy order-level fallback.
    @Query("SELECT AVG(CAST(r.rating AS double)) " +
            "FROM Review r " +
            "WHERE (r.orderItem IS NOT NULL AND r.orderItem.product.productId = :productId) " +
            "OR (r.orderItem IS NULL AND EXISTS (" +
            "  SELECT oi.orderItemId FROM OrderItem oi " +
            "  JOIN oi.seller s " +
            "  JOIN s.user sellerUser " +
            "  WHERE oi.order.orderId = r.order.orderId " +
            "  AND oi.product.productId = :productId " +
            "  AND sellerUser.userId = r.targetSeller.userId" +
            "))")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // Returns the total review count for a specific product using the same matching rules as product review listing.
    @Query("SELECT COUNT(r) " +
            "FROM Review r " +
            "WHERE (r.orderItem IS NOT NULL AND r.orderItem.product.productId = :productId) " +
            "OR (r.orderItem IS NULL AND EXISTS (" +
            "  SELECT oi.orderItemId FROM OrderItem oi " +
            "  JOIN oi.seller s " +
            "  JOIN s.user sellerUser " +
            "  WHERE oi.order.orderId = r.order.orderId " +
            "  AND oi.product.productId = :productId " +
            "  AND sellerUser.userId = r.targetSeller.userId" +
            "))")
    long countByProductId(@Param("productId") Long productId);

    // Returns all reviews for a specific sold product. Legacy order-level reviews fall back to order + seller matching.
    @Query("SELECT new com.ahsmart.campusmarket.payloadDTOs.review.ProductReviewDTO(" +
            "r.reviewId, r.reviewer.firstName, r.reviewer.lastName, r.rating, r.comment, r.createdAt) " +
            "FROM Review r " +
            "WHERE (r.orderItem IS NOT NULL AND r.orderItem.product.productId = :productId) " +
            "OR (r.orderItem IS NULL AND EXISTS (" +
            "  SELECT oi.orderItemId FROM OrderItem oi " +
            "  JOIN oi.seller s " +
            "  JOIN s.user sellerUser " +
            "  WHERE oi.order.orderId = r.order.orderId " +
            "  AND oi.product.productId = :productId " +
            "  AND sellerUser.userId = r.targetSeller.userId" +
            ")) " +
            "ORDER BY r.createdAt DESC")
    List<ProductReviewDTO> findProductReviewsByProductId(@Param("productId") Long productId);
}
