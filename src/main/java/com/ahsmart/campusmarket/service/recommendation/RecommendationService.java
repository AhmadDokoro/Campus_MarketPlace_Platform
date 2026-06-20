package com.ahsmart.campusmarket.service.recommendation;

import com.ahsmart.campusmarket.model.Product;

import java.util.List;

public interface RecommendationService {

    List<Product> getRecommendations(Long productId, int limit);

    // Ranks every embedded product by cosine similarity against an arbitrary query embedding and
    // returns the top {@code limit} products (images/category fetched). Powers the AI Marketplace
    // Analyst's semantic product search from a buyer's free-text request.
    List<Product> searchByEmbedding(List<Double> queryEmbedding, int limit);

    // Same semantic ranking as {@link #searchByEmbedding}, but keeps each product's cosine-similarity
    // score so callers can expose how strongly a result matches the query (e.g. the analyst's
    // "match strength" meter). Results stay ordered best-first.
    List<ProductMatch> searchByEmbeddingScored(List<Double> queryEmbedding, int limit);
}
