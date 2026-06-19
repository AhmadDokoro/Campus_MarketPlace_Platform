package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.payloadDTOs.ai.AnalystRequest;
import com.ahsmart.campusmarket.payloadDTOs.ai.AnalystResponse;
import com.ahsmart.campusmarket.service.analyst.MarketplaceAnalystService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JSON API behind the buyer-facing AI Marketplace Analyst widget.
 * <p>
 * The widget posts the buyer's free-text request here; the service performs an embedding search
 * over real products and asks the AI advisor to curate and explain the top picks. Read-only product
 * discovery, so it is open to any visitor browsing the buyer pages (the widget itself is hidden
 * from admins on the frontend).
 */
@RestController
@RequestMapping("/api/analyst")
public class MarketplaceAnalystController {

    private final MarketplaceAnalystService analystService;

    public MarketplaceAnalystController(MarketplaceAnalystService analystService) {
        this.analystService = analystService;
    }

    // Returns up to 3 curated, real-product recommendations for the buyer's request.
    @PostMapping("/recommend")
    public ResponseEntity<AnalystResponse> recommend(@RequestBody(required = false) AnalystRequest request) {
        String query = (request == null) ? null : request.query();
        return ResponseEntity.ok(analystService.analyze(query));
    }
}
