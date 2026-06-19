package com.ahsmart.campusmarket.service.analyst;

import com.ahsmart.campusmarket.payloadDTOs.ai.AnalystResponse;

/**
 * AI Marketplace Analyst — the buyer's intelligent shopping companion.
 * <p>
 * Turns a free-text shopping request into a small, curated set of real marketplace products using
 * embedding similarity search followed by an AI advisor pass that explains each pick. The advisor
 * may only choose from products that actually exist in the database; it never invents listings.
 */
public interface MarketplaceAnalystService {

    // Analyses the buyer's request and returns up to 3 curated product recommendations.
    AnalystResponse analyze(String userQuery);
}
