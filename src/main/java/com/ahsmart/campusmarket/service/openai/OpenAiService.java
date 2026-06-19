package com.ahsmart.campusmarket.service.openai;

import com.ahsmart.campusmarket.payloadDTOs.ai.FraudAnalysisResult;

import java.math.BigDecimal;

public interface OpenAiService {

    // Analyses a product listing and returns its classification (VERIFIED / SUSPICIOUS / UNKNOWN
    // fallback) together with a confidence score and the reasons behind the verdict.
    FraudAnalysisResult detectFraud(String title, String description, BigDecimal price);
}
