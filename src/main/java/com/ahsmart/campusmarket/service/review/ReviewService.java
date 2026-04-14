package com.ahsmart.campusmarket.service.review;

import com.ahsmart.campusmarket.payloadDTOs.review.ProductReviewDTO;

import java.util.List;
import java.util.Set;

public interface ReviewService {

    // Creates a review for the seller of the given order item after the buyer marks it as received.
    void createReview(Long orderItemId, Integer rating, String comment, Long buyerUserId);

    // Returns the set of order IDs already reviewed by this buyer (for profile UI display).
    Set<Long> getReviewedOrderIds(Long buyerUserId);

    // Returns all reviews from orders that contained this product — for the seller sales-history modal.
    List<ProductReviewDTO> getReviewsByProductId(Long productId);
}
