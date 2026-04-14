package com.ahsmart.campusmarket.helper;

import com.ahsmart.campusmarket.payloadDTOs.review.SellerRatingData;
import com.ahsmart.campusmarket.repositories.ReviewRepository;
import org.springframework.stereotype.Component;

// Thymeleaf-accessible Spring bean for resolving real seller star ratings.
// Usage in templates: ${@sellerRatingHelper.getRatingData(p.seller.user.userId)}
@Component("sellerRatingHelper")
public class SellerRatingHelper {

    private final ReviewRepository reviewRepository;

    public SellerRatingHelper(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // Returns the aggregate rating data for a seller identified by their user ID.
    public SellerRatingData getRatingData(Long sellerUserId) {
        if (sellerUserId == null) {
            return new SellerRatingData(0.0, 0L);
        }
        Double avg = reviewRepository.findAverageRatingBySellerUserId(sellerUserId);
        long count = reviewRepository.countByTargetSeller_UserId(sellerUserId);
        return new SellerRatingData(avg == null ? 0.0 : avg, count);
    }
}
