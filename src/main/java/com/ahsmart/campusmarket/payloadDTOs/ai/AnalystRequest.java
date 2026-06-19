package com.ahsmart.campusmarket.payloadDTOs.ai;

/**
 * Inbound payload for the AI Marketplace Analyst.
 * Carries the buyer's free-text shopping request (from a suggestion chip or the text box).
 */
public record AnalystRequest(String query) {
}
