package com.ahsmart.campusmarket.service.recommendation;

import com.ahsmart.campusmarket.model.Product;

import java.util.List;

public interface RecommendationService {

    List<Product> getRecommendations(Long productId, int limit);
}
