package com.ahsmart.campusmarket.payloadDTOs.review;

// Immutable carrier for a product's aggregate rating data.
public record ProductRatingData(double averageRating, long reviewCount) {}
