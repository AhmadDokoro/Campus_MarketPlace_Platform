package com.ahsmart.campusmarket.helper;

import com.ahsmart.campusmarket.payloadDTOs.review.ProductRatingData;
import com.ahsmart.campusmarket.service.review.ReviewService;
import org.springframework.stereotype.Component;

// Thymeleaf-accessible Spring bean for resolving product-specific star ratings.
@Component("productRatingHelper")
public class ProductRatingHelper {

    private final ReviewService reviewService;

    public ProductRatingHelper(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    public ProductRatingData getRatingData(Long productId) {
        return reviewService.getProductRatingData(productId);
    }
}
