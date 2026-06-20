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
        return rankAndFetch(currentEmbedding, candidates, limit).stream()
                .map(ProductMatch::product)
                .toList();
    }

    @Override
    public List<Product> searchByEmbedding(List<Double> queryEmbedding, int limit) {
        return searchByEmbeddingScored(queryEmbedding, limit).stream()
                .map(ProductMatch::product)
                .toList();
    }

    @Override
    public List<ProductMatch> searchByEmbeddingScored(List<Double> queryEmbedding, int limit) {
        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            return Collections.emptyList();
        }
        List<Object[]> candidates = productRepository.findAllProductEmbeddings();
        return rankAndFetch(queryEmbedding, candidates, limit);
    }

    // Shared ranking: score each candidate by cosine similarity to the reference embedding, keep
    // the top {@code limit}, then load those products (with images/category) preserving rank order.
    // Each result keeps its similarity score so callers can expose match strength when useful.
    private List<ProductMatch> rankAndFetch(List<Double> referenceEmbedding, List<Object[]> candidates, int limit) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredProduct> scored = new ArrayList<>();
        for (Object[] row : candidates) {
            Long candidateId = (Long) row[0];
            String embeddingJson = (String) row[1];
            try {
                List<Double> candidateEmbedding = embeddingService.fromJson(embeddingJson);
                double similarity = cosineSimilarity(referenceEmbedding, candidateEmbedding);
                scored.add(new ScoredProduct(candidateId, similarity));
            } catch (Exception e) {
                log.warn("Skipping product {} due to invalid embedding: {}", candidateId, e.getMessage());
            }
        }

        scored.sort(Comparator.comparingDouble(ScoredProduct::score).reversed());

        List<ScoredProduct> top = scored.stream().limit(limit).toList();
        if (top.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> topIds = top.stream().map(ScoredProduct::productId).toList();
        List<Product> products = productRepository.findByProductIdInWithImages(topIds);

        Map<Long, Product> byId = new HashMap<>();
        for (Product p : products) {
            byId.put(p.getProductId(), p);
        }
        List<ProductMatch> ordered = new ArrayList<>();
        for (ScoredProduct s : top) {
            Product p = byId.get(s.productId());
            if (p != null) ordered.add(new ProductMatch(p, s.score()));
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
