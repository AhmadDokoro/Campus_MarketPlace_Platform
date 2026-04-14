package com.ahsmart.campusmarket.payloadDTOs.review;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Carries review data for a product — used in the seller sales-history modal (JSON response).
@Getter
public class ProductReviewDTO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final Long reviewId;
    private final String reviewerName;
    private final Integer rating;
    private final String comment;
    private final String createdAt;

    public ProductReviewDTO(Long reviewId,
                            String reviewerFirstName,
                            String reviewerLastName,
                            Integer rating,
                            String comment,
                            LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.reviewerName = buildName(reviewerFirstName, reviewerLastName);
        this.rating = rating;
        this.comment = comment == null ? "" : comment;
        this.createdAt = createdAt != null ? createdAt.format(FORMATTER) : "";
    }

    private static String buildName(String first, String last) {
        String f = first == null ? "" : first.trim();
        String l = last == null ? "" : last.trim();
        String full = (f + " " + l).trim();
        return full.isEmpty() ? "Anonymous" : full;
    }
}
