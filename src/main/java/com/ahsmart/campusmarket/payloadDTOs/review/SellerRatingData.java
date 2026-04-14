package com.ahsmart.campusmarket.payloadDTOs.review;

// Immutable carrier for a seller's aggregate rating data — used by SellerRatingHelper in Thymeleaf.
public record SellerRatingData(double averageRating, long reviewCount) {}
