package com.ahsmart.campusmarket.service.review;

import com.ahsmart.campusmarket.payloadDTOs.review.ProductReviewDTO;
import com.ahsmart.campusmarket.payloadDTOs.review.ProductRatingData;

import java.util.List;
import java.util.Set;

public interface ReviewService {

    // Creates a review for the seller of the given order item after the buyer marks it as received.
    void createReview(Long orderItemId, Integer rating, String comment, Long buyerUserId);

    // Returns the set of order item IDs already reviewed by this buyer (for profile UI display).
    Set<Long> getReviewedOrderItemIds(Long buyerUserId);

    // Returns all reviews from orders that contained this product — for the seller sales-history modal.
    List<ProductReviewDTO> getReviewsByProductId(Long productId);

    // Returns product-specific rating summary data for cards and the detail page.
    ProductRatingData getProductRatingData(Long productId);
}
