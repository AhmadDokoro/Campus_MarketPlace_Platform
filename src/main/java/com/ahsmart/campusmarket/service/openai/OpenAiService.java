package com.ahsmart.campusmarket.service.openai;

import com.ahsmart.campusmarket.payloadDTOs.ai.FraudAnalysisResult;

import java.math.BigDecimal;

public interface OpenAiService {

    // Analyses a product listing and returns its classification (VERIFIED / SUSPICIOUS / UNKNOWN
    // fallback) together with a confidence score and the reasons behind the verdict.
    FraudAnalysisResult detectFraud(String title, String description, BigDecimal price);

    // Generic chat-completion helper that forces a JSON-object response and returns the raw
    // assistant message content (or {@code null} when the API is unavailable). Reused by features
    // such as the AI Marketplace Analyst that need structured JSON back from the model.
    String chatJson(String systemPrompt, String userPrompt, double temperature, int maxTokens);
}
