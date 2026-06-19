package com.ahsmart.campusmarket.payloadDTOs.ai;

import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Immutable result of an OpenAI fraud analysis call.
 * <p>
 * Bundles the classification ({@link FlaggedStatus}) with the supporting confidence score and
 * reason list. The {@code status} is persisted to {@code products.flagged_status}; {@code confidence}
 * and {@code reasons} are persisted to {@code product_ai_analysis}.
 */
@Getter
public class FraudAnalysisResult {

    private final FlaggedStatus status;
    private final Integer confidence;       // 0–100, null when AI did not produce a score
    private final List<String> reasons;     // never null

    public FraudAnalysisResult(FlaggedStatus status, Integer confidence, List<String> reasons) {
        this.status = status == null ? FlaggedStatus.UNKNOWN : status;
        this.confidence = confidence;
        this.reasons = reasons == null ? Collections.emptyList() : reasons;
    }

    // Fallback used when the AI call fails or returns nothing parseable — preserves the legacy
    // "default to UNKNOWN" behaviour and carries no analysis metadata.
    public static FraudAnalysisResult unknown() {
        return new FraudAnalysisResult(FlaggedStatus.UNKNOWN, null, Collections.emptyList());
    }

    // True when the call produced a real classification worth persisting to product_ai_analysis.
    public boolean isAnalysisAvailable() {
        return status == FlaggedStatus.VERIFIED || status == FlaggedStatus.SUSPICIOUS;
    }
}
