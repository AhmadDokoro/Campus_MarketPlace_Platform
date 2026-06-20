package com.ahsmart.campusmarket.service.recommendation;

import com.ahsmart.campusmarket.model.Product;

/**
 * A product paired with its cosine-similarity score against a reference embedding.
 * <p>
 * Carries the raw semantic similarity ({@code 0.0}–{@code 1.0}) alongside the product so callers —
 * notably the AI Marketplace Analyst — can surface how strongly each result matches the buyer's
 * request, instead of discarding the score after ranking.
 */
public record ProductMatch(Product product, double similarity) {
}
