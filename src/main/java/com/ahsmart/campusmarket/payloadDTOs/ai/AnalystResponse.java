package com.ahsmart.campusmarket.payloadDTOs.ai;

import java.util.Collections;
import java.util.List;

/**
 * Structured response for the AI Marketplace Analyst widget.
 * <p>
 * {@code success} signals whether the request was understood and processed. {@code intro} is a
 * short, friendly one-liner shown above the cards. {@code message} carries a user-facing note
 * for the empty / error / off-topic cases. {@code recommendations} holds the top (max 3) curated
 * product cards — always backed by real database products.
 */
public record AnalystResponse(
        boolean success,
        String intro,
        String message,
        List<AnalystRecommendation> recommendations
) {

    // Successful curation with at least one product card.
    public static AnalystResponse of(String intro, List<AnalystRecommendation> recommendations) {
        return new AnalystResponse(true, intro, null, recommendations);
    }

    // Understood the request but found nothing relevant in the marketplace.
    public static AnalystResponse empty(String message) {
        return new AnalystResponse(true, null, message, Collections.emptyList());
    }

    // The request could not be processed (blank query, AI/embedding outage, etc.).
    public static AnalystResponse error(String message) {
        return new AnalystResponse(false, null, message, Collections.emptyList());
    }
}
