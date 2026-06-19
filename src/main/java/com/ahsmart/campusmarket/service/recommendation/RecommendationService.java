package com.ahsmart.campusmarket.service.recommendation;

import com.ahsmart.campusmarket.model.Product;

import java.util.List;

public interface RecommendationService {

    List<Product> getRecommendations(Long productId, int limit);

    // Ranks every embedded product by cosine similarity against an arbitrary query embedding and
    // returns the top {@code limit} products (images/category fetched). Powers the AI Marketplace
    // Analyst's semantic product search from a buyer's free-text request.
    List<Product> searchByEmbedding(List<Double> queryEmbedding, int limit);
}
