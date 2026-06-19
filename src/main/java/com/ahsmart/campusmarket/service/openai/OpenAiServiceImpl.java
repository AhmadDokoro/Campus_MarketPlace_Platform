package com.ahsmart.campusmarket.service.openai;

import com.ahsmart.campusmarket.helper.OpenAiConfig;
import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import com.ahsmart.campusmarket.payloadDTOs.ai.FraudAnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenAiServiceImpl implements OpenAiService {

    // Fraud detection system prompt — instructs the model to output a structured JSON verdict.
    private static final String SYSTEM_PROMPT =
            "You are a fraud detection system for a campus marketplace platform used by university students. " +
            "Your job is to analyse product listings and identify potential scams, misleading items, or suspicious pricing.\n\n" +
            "Classify the listing as SUSPICIOUS if any of the following apply:\n" +
            "- The price is unrealistically low or high for the type of item described\n" +
            "- The title or description contains vague, misleading, or deceptive language\n" +
            "- The combination of title, description, and price is inconsistent or suggests a scam\n" +
            "- The description does not match the product title\n\n" +
            "Classify the listing as VERIFIED if it appears to be a genuine, reasonably priced product " +
            "typical of a student campus marketplace.\n\n" +
            "Respond with ONLY a JSON object in EXACTLY this shape and nothing else:\n" +
            "{\"status\": \"VERIFIED\" | \"SUSPICIOUS\", \"confidence\": <integer 0-100>, \"reasons\": [<short strings>]}\n\n" +
            "Rules:\n" +
            "- \"status\" must be exactly VERIFIED or SUSPICIOUS.\n" +
            "- \"confidence\" is an integer 0-100 expressing how confident you are in the status.\n" +
            "- \"reasons\" is an array of short, specific strings explaining the verdict. " +
            "For a clean VERIFIED listing with no concerns, return an empty array [].\n" +
            "- Do not include markdown, code fences, or any text outside the JSON object.";

    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiServiceImpl(OpenAiConfig openAiConfig) {
        this.openAiConfig = openAiConfig;
    }

    @Override
    public FraudAnalysisResult detectFraud(String title, String description, BigDecimal price) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model",       OpenAiConfig.MODEL);
            body.put("temperature", 0.1);
            body.put("max_tokens",  300);
            // Ask the API to guarantee a JSON object response so parsing is robust.
            body.put("response_format", Map.of("type", "json_object"));
            body.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user",   "content", buildUserMessage(title, description, price))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiConfig.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    OpenAiConfig.API_URL, request, Map.class
            );

            return parseAnalysis(response.getBody());

        } catch (Exception e) {
            log.warn("OpenAI fraud detection unavailable, defaulting to UNKNOWN: {}", e.getMessage());
            return FraudAnalysisResult.unknown();
        }
    }

    // Generic JSON chat completion. Mirrors the request shape used by detectFraud (same model,
    // same json_object response_format) but lets callers supply their own prompts and limits.
    // Returns the raw assistant content string, or null when the call fails for any reason.
    @Override
    public String chatJson(String systemPrompt, String userPrompt, double temperature, int maxTokens) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model",       OpenAiConfig.MODEL);
            body.put("temperature", temperature);
            body.put("max_tokens",  maxTokens);
            body.put("response_format", Map.of("type", "json_object"));
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user",   "content", userPrompt)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiConfig.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    OpenAiConfig.API_URL, request, Map.class
            );

            return extractContent(response.getBody());

        } catch (Exception e) {
            log.warn("OpenAI chatJson call failed: {}", e.getMessage());
            return null;
        }
    }

    // Pulls choices[0].message.content out of the chat-completion envelope; null on any surprise.
    @SuppressWarnings("unchecked")
    private String extractContent(Map<?, ?> body) {
        if (body == null) return null;
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) return null;
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) return null;
        Object content = message.get("content");
        return content == null ? null : String.valueOf(content);
    }

    private String buildUserMessage(String title, String description, BigDecimal price) {
        String desc = (description != null && !description.isBlank())
                ? description
                : "(no description provided)";
        return String.format("Product Title: %s%nDescription: %s%nPrice: RM %.2f", title, desc, price);
    }

    // Extracts the assistant message content from the chat-completion envelope, then parses the
    // embedded JSON verdict. Any structural surprise falls back to UNKNOWN rather than throwing.
    @SuppressWarnings("unchecked")
    private FraudAnalysisResult parseAnalysis(Map<?, ?> body) {
        if (body == null) return FraudAnalysisResult.unknown();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) return FraudAnalysisResult.unknown();
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) return FraudAnalysisResult.unknown();
        String content = String.valueOf(message.get("content"));
        return parseVerdictJson(content);
    }

    // Robustly parses the model's JSON verdict. Tolerates code fences and stray surrounding text;
    // if the JSON is unusable it falls back to keyword detection on the raw content.
    private FraudAnalysisResult parseVerdictJson(String content) {
        if (content == null || content.isBlank()) return FraudAnalysisResult.unknown();

        String json = extractJsonObject(content);
        try {
            JsonNode root = objectMapper.readTree(json);

            FlaggedStatus status = parseStatus(root.path("status").asText(null));
            if (status == FlaggedStatus.UNKNOWN) {
                // No usable status field — fall back to scanning the raw text.
                status = keywordStatus(content);
            }
            if (status == FlaggedStatus.UNKNOWN) return FraudAnalysisResult.unknown();

            Integer confidence = parseConfidence(root.path("confidence"));
            List<String> reasons = parseReasons(root.path("reasons"));
            return new FraudAnalysisResult(status, confidence, reasons);

        } catch (Exception e) {
            // Malformed JSON: degrade gracefully to a status-only verdict via keyword scan.
            FlaggedStatus status = keywordStatus(content);
            if (status == FlaggedStatus.UNKNOWN) return FraudAnalysisResult.unknown();
            return new FraudAnalysisResult(status, null, new ArrayList<>());
        }
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

    private FlaggedStatus parseStatus(String raw) {
        if (raw == null) return FlaggedStatus.UNKNOWN;
        String upper = raw.trim().toUpperCase();
        if (upper.contains("SUSPICIOUS")) return FlaggedStatus.SUSPICIOUS;
        if (upper.contains("VERIFIED"))   return FlaggedStatus.VERIFIED;
        return FlaggedStatus.UNKNOWN;
    }

    private FlaggedStatus keywordStatus(String content) {
        String upper = content.toUpperCase();
        if (upper.contains("SUSPICIOUS")) return FlaggedStatus.SUSPICIOUS;
        if (upper.contains("VERIFIED"))   return FlaggedStatus.VERIFIED;
        return FlaggedStatus.UNKNOWN;
    }

    // Accepts integers, decimals, or numeric strings; clamps to 0–100; null when absent/invalid.
    private Integer parseConfidence(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        try {
            int value;
            if (node.isNumber()) {
                value = (int) Math.round(node.asDouble());
            } else {
                String text = node.asText().trim().replace("%", "");
                if (text.isEmpty()) return null;
                value = (int) Math.round(Double.parseDouble(text));
            }
            return Math.max(0, Math.min(100, value));
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
}
