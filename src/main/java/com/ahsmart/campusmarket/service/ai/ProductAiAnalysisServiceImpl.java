package com.ahsmart.campusmarket.service.ai;

import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.ProductAiAnalysis;
import com.ahsmart.campusmarket.repositories.ProductAiAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductAiAnalysisServiceImpl implements ProductAiAnalysisService {

    private final ProductAiAnalysisRepository productAiAnalysisRepository;

    // Upsert keyed by product so the unique product_id constraint is never violated: an existing
    // row is updated in place, otherwise a new one is created.
    @Override
    @Transactional
    public ProductAiAnalysis saveAnalysis(Product product, Integer confidenceScore, List<String> reasons) {
        ProductAiAnalysis analysis = productAiAnalysisRepository
                .findByProduct_ProductId(product.getProductId())
                .orElseGet(ProductAiAnalysis::new);

        analysis.setProduct(product);
        analysis.setConfidenceScore(confidenceScore);
        analysis.setReasons(reasons == null ? new ArrayList<>() : new ArrayList<>(reasons));

        return productAiAnalysisRepository.save(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductAiAnalysis> getByProductId(Long productId) {
        return productAiAnalysisRepository.findByProduct_ProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ProductAiAnalysis> getByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, ProductAiAnalysis> byProductId = new LinkedHashMap<>();
        for (ProductAiAnalysis analysis : productAiAnalysisRepository.findByProduct_ProductIdIn(productIds)) {
            byProductId.put(analysis.getProduct().getProductId(), analysis);
        }
        return byProductId;
    }
}
