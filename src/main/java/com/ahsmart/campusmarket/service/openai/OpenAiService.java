package com.ahsmart.campusmarket.service.openai;

import com.ahsmart.campusmarket.model.enums.FlaggedStatus;

import java.math.BigDecimal;

public interface OpenAiService {

    // Analyses a product listing and returns VERIFIED, SUSPICIOUS, or UNKNOWN (API fallback).
    FlaggedStatus detectFraud(String title, String description, BigDecimal price);
}
