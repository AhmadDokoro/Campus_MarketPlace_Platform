package com.ahsmart.campusmarket.service.ai;

import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.ProductAiAnalysis;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductAiAnalysisService {

    // Creates or updates (one row per product) the AI analysis metadata for a product.
    ProductAiAnalysis saveAnalysis(Product product, Integer confidenceScore, List<String> reasons);

    // Loads the AI analysis for a single product, if one was recorded.
    Optional<ProductAiAnalysis> getByProductId(Long productId);

    // Bulk lookup keyed by product id — used by the admin flagged-products list.
    Map<Long, ProductAiAnalysis> getByProductIds(List<Long> productIds);
}
