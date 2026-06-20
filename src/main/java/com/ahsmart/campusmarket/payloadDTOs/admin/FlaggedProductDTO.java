package com.ahsmart.campusmarket.payloadDTOs.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class FlaggedProductDTO {
    private final Long productId;
    private final String title;
    private final String description;
    private final BigDecimal price;
    private final Integer quantity;
    private final String conditionName;
    private final String categoryName;
    private final String sellerName;
    private final String primaryImageUrl;
    private final LocalDateTime createdAt;

    // AI scam-analysis metadata from product_ai_analysis. Null/empty when no analysis exists.
    private final Integer aiConfidenceScore;
    private final List<String> aiReasons;

    // True when an AI analysis row was found for this product (drives the AI card in the template).
    public boolean isAiAnalysisAvailable() {
        return aiConfidenceScore != null || (aiReasons != null && !aiReasons.isEmpty());
    }

    // Never-null accessor for safe iteration in the template.
    public List<String> getAiReasons() {
        return aiReasons == null ? Collections.emptyList() : aiReasons;
    }
}
