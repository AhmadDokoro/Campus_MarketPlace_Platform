package com.ahsmart.campusmarket.service.recommendation;

import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.service.embedding.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final ProductRepository productRepository;
    private final EmbeddingService embeddingService;

    @Override
    public List<Product> getRecommendations(Long productId, int limit) {
        Product currentProduct = productRepository.findById(productId).orElse(null);
        if (currentProduct == null || currentProduct.getEmbedding() == null) {
            return Collections.emptyList();
        }

        List<Double> currentEmbedding = embeddingService.fromJson(currentProduct.getEmbedding());

        List<Object[]> candidates = productRepository.findAllEmbeddings(productId);
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredProduct> scored = new ArrayList<>();
        for (Object[] row : candidates) {
            Long candidateId = (Long) row[0];
            String embeddingJson = (String) row[1];
            try {
                List<Double> candidateEmbedding = embeddingService.fromJson(embeddingJson);
                double similarity = cosineSimilarity(currentEmbedding, candidateEmbedding);
                scored.add(new ScoredProduct(candidateId, similarity));
            } catch (Exception e) {
                log.warn("Skipping product {} due to invalid embedding: {}", candidateId, e.getMessage());
            }
        }

        scored.sort(Comparator.comparingDouble(ScoredProduct::score).reversed());

        List<Long> topIds = scored.stream()
                .limit(limit)
                .map(ScoredProduct::productId)
                .toList();

        if (topIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Product> products = productRepository.findByProductIdInWithImages(topIds);

        Map<Long, Product> byId = new HashMap<>();
        for (Product p : products) {
            byId.put(p.getProductId(), p);
        }
        List<Product> ordered = new ArrayList<>();
        for (Long id : topIds) {
            Product p = byId.get(id);
            if (p != null) ordered.add(p);
        }
        return ordered;
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a.size() != b.size()) return 0.0;
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0.0 ? 0.0 : dotProduct / denominator;
    }

    private record ScoredProduct(Long productId, double score) {}
}
