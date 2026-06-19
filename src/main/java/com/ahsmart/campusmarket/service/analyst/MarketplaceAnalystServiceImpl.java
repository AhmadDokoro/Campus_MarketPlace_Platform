package com.ahsmart.campusmarket.service.analyst;

import com.ahsmart.campusmarket.helper.CloudinaryUrlHelper;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.ProductImage;
import com.ahsmart.campusmarket.payloadDTOs.ai.AnalystRecommendation;
import com.ahsmart.campusmarket.payloadDTOs.ai.AnalystResponse;
import com.ahsmart.campusmarket.service.embedding.EmbeddingService;
import com.ahsmart.campusmarket.service.openai.OpenAiService;
import com.ahsmart.campusmarket.service.recommendation.RecommendationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceAnalystServiceImpl implements MarketplaceAnalystService {

    // How many semantically-closest products to hand to the AI advisor, and how many it returns.
    private static final int CANDIDATE_POOL = 8;
    private static final int MAX_RECOMMENDATIONS = 3;
    private static final int MAX_DESC_CHARS = 180;
    private static final int MAX_QUERY_CHARS = 400;

    // Friendly, on-brand labels used as a fallback / when the model omits one.
    private static final String[] DEFAULT_LABELS = {"Best Match", "Alternative Choice", "Budget Option"};

    // The AI advisor persona + strict, anti-hallucination output contract.
    private static final String SYSTEM_PROMPT =
            "You are the AI Marketplace Analyst for a university campus marketplace where students buy and " +
            "sell items. You act like a helpful, honest shop owner — never a pushy salesperson. Your ONLY job " +
            "is to help the buyer discover the most suitable products for their request.\n\n" +
            "You are given the buyer's request and a numbered list of REAL products from the marketplace " +
            "(each with an id, title, price in RM, category, condition and short description).\n\n" +
            "Rules:\n" +
            "- Recommend ONLY products from the provided list. NEVER invent products, prices, or details.\n" +
            "- Select the best " + MAX_RECOMMENDATIONS + " products (fewer if fewer are genuinely relevant). " +
            "Order them best-first.\n" +
            "- Use ONLY the productId values from the list to refer to products.\n" +
            "- For each pick, give 2-3 short, specific reasons explaining WHY it suits the request " +
            "(fit for purpose, value for the budget, condition, features). Be factual, not promotional.\n" +
            "- If the request is NOT about shopping for products (e.g. general knowledge, chit-chat), or none " +
            "of the products are relevant, return an empty \"recommendations\" array and a short polite " +
            "\"intro\" explaining you can only help find products in this marketplace.\n\n" +
            "Respond with ONLY a JSON object in EXACTLY this shape and nothing else:\n" +
            "{\"intro\": <one short friendly sentence>, \"recommendations\": [{\"productId\": <number>, " +
            "\"label\": <\"Best Match\"|\"Alternative Choice\"|\"Budget Option\"|short label>, " +
            "\"reasons\": [<short strings>]}]}\n" +
            "Do not include markdown, code fences, or any text outside the JSON object.";

    private final EmbeddingService embeddingService;
    private final RecommendationService recommendationService;
    private final OpenAiService openAiService;
    private final CloudinaryUrlHelper cloudinaryUrlHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AnalystResponse analyze(String userQuery) {
        // 1. Validate the buyer's request.
        if (userQuery == null || userQuery.isBlank()) {
            return AnalystResponse.error("Tell me what you're looking for and I'll scan the marketplace for you.");
        }
        String query = userQuery.trim();
        if (query.length() > MAX_QUERY_CHARS) {
            query = query.substring(0, MAX_QUERY_CHARS);
        }

        // 2. Embed the request and 3-4. retrieve the most semantically relevant products.
        List<Product> candidates;
        try {
            List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
            candidates = recommendationService.searchByEmbedding(queryEmbedding, CANDIDATE_POOL);
        } catch (Exception e) {
            log.warn("Analyst embedding search failed: {}", e.getMessage());
            return AnalystResponse.error("The analyst is taking a short break. Please try again in a moment.");
        }

        if (candidates.isEmpty()) {
            return AnalystResponse.empty("I couldn't find matching products in the marketplace yet. " +
                    "Try a different search, like \"calculator\" or \"laptop\".");
        }

        // Index the candidates so we can map AI picks back to real products (never trust AI-invented ids).
        Map<Long, Product> candidatesById = new LinkedHashMap<>();
        for (Product p : candidates) {
            candidatesById.put(p.getProductId(), p);
        }

        // 5-6. Ask the AI advisor to curate + explain, constrained to the candidate products.
        String userPrompt = buildUserPrompt(query, candidates);
        String aiContent = openAiService.chatJson(SYSTEM_PROMPT, userPrompt, 0.3, 700);

        // 7. Parse + validate the AI verdict against the real candidate set.
        Curation curation = parseCuration(aiContent, candidatesById);

        // 8. Decide how to respond when the curation produced no cards.
        if (curation.recommendations.isEmpty()) {
            // The model returned a genuinely empty list (off-topic / nothing relevant) — relay its note.
            if (curation.intentionalEmpty && curation.rawRecCount == 0) {
                String msg = (curation.intro != null && !curation.intro.isBlank())
                        ? curation.intro
                        : "I can only help you find products in the campus marketplace. Try searching for an item.";
                return AnalystResponse.empty(msg);
            }
            // AI unavailable, unparseable, or its picks were all invalid — fall back to the top
            // embedding matches so the buyer still gets a real, useful result.
            return buildFallback(candidates);
        }

        String intro = (curation.intro != null && !curation.intro.isBlank())
                ? curation.intro
                : "Here's what I found in the marketplace for you:";
        return AnalystResponse.of(intro, curation.recommendations);
    }

    // ── Prompt building ─────────────────────────────────────────────────────────────────────────

    private String buildUserPrompt(String query, List<Product> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("Buyer request: \"").append(query).append("\"\n\n");
        sb.append("Available products (").append(candidates.size()).append("):\n");
        for (Product p : candidates) {
            sb.append("- productId: ").append(p.getProductId())
              .append(" | title: ").append(p.getTitle())
              .append(" | price: RM ").append(p.getPrice())
              .append(" | category: ").append(categoryName(p))
              .append(" | condition: ").append(p.getCondition() != null ? p.getCondition().name() : "N/A")
              .append(" | description: ").append(shortDescription(p))
              .append('\n');
        }
        return sb.toString();
    }

    private String categoryName(Product p) {
        return (p.getCategory() != null && p.getCategory().getCategoryName() != null)
                ? p.getCategory().getCategoryName() : "Uncategorised";
    }

    private String shortDescription(Product p) {
        String desc = p.getDescription();
        if (desc == null || desc.isBlank()) return "(none)";
        desc = desc.replaceAll("\\s+", " ").trim();
        return desc.length() > MAX_DESC_CHARS ? desc.substring(0, MAX_DESC_CHARS) + "…" : desc;
    }

    // ── AI response parsing ─────────────────────────────────────────────────────────────────────

    private Curation parseCuration(String content, Map<Long, Product> candidatesById) {
        Curation curation = new Curation();
        if (content == null || content.isBlank()) {
            return curation; // triggers fallback
        }
        try {
            JsonNode root = objectMapper.readTree(extractJsonObject(content));
            curation.intro = root.path("intro").asText(null);

            JsonNode recs = root.path("recommendations");
            if (recs.isArray()) {
                // The model produced a valid array — even an empty one is an intentional decision.
                curation.intentionalEmpty = true;
                curation.rawRecCount = recs.size();
                int idx = 0;
                for (JsonNode rec : recs) {
                    if (curation.recommendations.size() >= MAX_RECOMMENDATIONS) break;
                    Long productId = parseProductId(rec.path("productId"));
                    Product product = (productId == null) ? null : candidatesById.get(productId);
                    if (product == null) continue; // ignore hallucinated / out-of-pool ids

                    String label = rec.path("label").asText(null);
                    if (label == null || label.isBlank()) {
                        label = DEFAULT_LABELS[Math.min(idx, DEFAULT_LABELS.length - 1)];
                    }
                    List<String> reasons = parseReasons(rec.path("reasons"));
                    curation.recommendations.add(toRecommendation(product, label, reasons));
                    idx++;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse analyst AI response, will fall back to top matches: {}", e.getMessage());
        }
        return curation;
    }

    private Long parseProductId(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        try {
            if (node.isNumber()) return node.asLong();
            String text = node.asText().trim();
            return text.isEmpty() ? null : Long.parseLong(text);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseReasons(JsonNode node) {
        List<String> reasons = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                String reason = item.asText(null);
                if (reason != null && !reason.isBlank()) {
                    reasons.add(reason.trim());
                }
            }
        }
        return reasons;
    }

    // Returns the substring from the first '{' to the last '}', stripping fences/prose around it.
    private String extractJsonObject(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content.trim();
    }

    // ── Fallback + mapping ──────────────────────────────────────────────────────────────────────

    // Used when the AI is unavailable or unparseable: present the top embedding matches directly so
    // the buyer still gets a useful, real, semantically-ranked result.
    private AnalystResponse buildFallback(List<Product> candidates) {
        List<AnalystRecommendation> recs = new ArrayList<>();
        int count = Math.min(MAX_RECOMMENDATIONS, candidates.size());
        for (int i = 0; i < count; i++) {
            Product p = candidates.get(i);
            List<String> reasons = new ArrayList<>();
            reasons.add("Closely matches what you searched for");
            reasons.add("Available now in the campus marketplace");
            recs.add(toRecommendation(p, DEFAULT_LABELS[Math.min(i, DEFAULT_LABELS.length - 1)], reasons));
        }
        return AnalystResponse.of("Here are the closest matches I found in the marketplace:", recs);
    }

    private AnalystRecommendation toRecommendation(Product p, String label, List<String> reasons) {
        return new AnalystRecommendation(
                p.getProductId(),
                p.getTitle(),
                p.getPrice(),
                resolveImageUrl(p),
                categoryName(p),
                label,
                reasons
        );
    }

    // Primary image first, then first non-empty image, optimised to a thumbnail; null when none.
    private String resolveImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        String chosen = null;
        for (ProductImage image : product.getImages()) {
            if (Boolean.TRUE.equals(image.getIsPrimary()) && hasUrl(image)) {
                chosen = image.getImageUrl();
                break;
            }
        }
        if (chosen == null) {
            for (ProductImage image : product.getImages()) {
                if (hasUrl(image)) {
                    chosen = image.getImageUrl();
                    break;
                }
            }
        }
        return chosen == null ? null : cloudinaryUrlHelper.thumbnail(chosen);
    }

    private boolean hasUrl(ProductImage image) {
        return image.getImageUrl() != null && !image.getImageUrl().isBlank();
    }

    // Mutable holder for a parsed AI curation pass.
    private static class Curation {
        String intro;
        boolean intentionalEmpty = false; // true when the model returned a valid (possibly empty) array
        int rawRecCount = 0;              // size of the model's raw recommendations array (pre-validation)
        final List<AnalystRecommendation> recommendations = new ArrayList<>();
    }
}
