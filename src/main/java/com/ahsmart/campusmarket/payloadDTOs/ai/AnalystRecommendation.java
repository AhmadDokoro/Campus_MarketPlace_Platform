package com.ahsmart.campusmarket.payloadDTOs.ai;

import java.math.BigDecimal;
import java.util.List;

/**
 * One curated product recommendation returned by the AI Marketplace Analyst.
 * <p>
 * The product fields ({@code productId}, {@code title}, {@code price}, {@code imageUrl},
 * {@code categoryName}) are sourced strictly from the database — the AI never invents them.
 * Only the {@code label} and {@code reasons} (the "why I selected this" explanation) come
 * from the model, and they describe a product that provably exists in the marketplace.
 * {@code matchScore} is a 0–100 relevance reading derived from the product's real semantic
 * (embedding) similarity to the buyer's request — it powers the widget's "match strength" meter.
 */
public record AnalystRecommendation(
        Long productId,
        String title,
        BigDecimal price,
        String imageUrl,
        String categoryName,
        String label,            // e.g. "Best Match", "Alternative Choice", "Budget Option"
        List<String> reasons,    // short bullet points explaining the selection
        Integer matchScore       // 0–100 semantic relevance, from embedding cosine similarity
) {
}
