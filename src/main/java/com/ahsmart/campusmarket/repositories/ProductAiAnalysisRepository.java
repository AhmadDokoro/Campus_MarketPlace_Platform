package com.ahsmart.campusmarket.repositories;

import com.ahsmart.campusmarket.model.ProductAiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAiAnalysisRepository extends JpaRepository<ProductAiAnalysis, Long> {

    // Loads the single AI analysis row for a product (unique product_id), if one exists.
    Optional<ProductAiAnalysis> findByProduct_ProductId(Long productId);

    // Bulk lookup for the admin flagged-products list — avoids an N+1 query per card.
    List<ProductAiAnalysis> findByProduct_ProductIdIn(List<Long> productIds);
}
