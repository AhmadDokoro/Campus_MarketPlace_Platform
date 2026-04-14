package com.ahsmart.campusmarket.service.openai;

import com.ahsmart.campusmarket.helper.OpenAiConfig;
import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenAiServiceImpl implements OpenAiService {

    // Fraud detection system prompt — instructs the model to output only VERIFIED or SUSPICIOUS.
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
            "Respond with ONLY one word: either VERIFIED or SUSPICIOUS. No explanation, no punctuation.";

    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public OpenAiServiceImpl(OpenAiConfig openAiConfig) {
        this.openAiConfig = openAiConfig;
    }

    @Override
    public FlaggedStatus detectFraud(String title, String description, BigDecimal price) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model",       OpenAiConfig.MODEL);
            body.put("temperature", 0.1);
            body.put("max_tokens",  10);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user",   "content", buildUserMessage(title, description, price))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiConfig.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    OpenAiConfig.API_URL, request, Map.class
            );

            return parseClassification(response.getBody());

        } catch (Exception e) {
            log.warn("OpenAI fraud detection unavailable, defaulting to UNKNOWN: {}", e.getMessage());
            return FlaggedStatus.UNKNOWN;
        }
    }

    private String buildUserMessage(String title, String description, BigDecimal price) {
        String desc = (description != null && !description.isBlank())
                ? description
                : "(no description provided)";
        return String.format("Product Title: %s%nDescription: %s%nPrice: RM %.2f", title, desc, price);
    }

    @SuppressWarnings("unchecked")
    private FlaggedStatus parseClassification(Map<?, ?> body) {
        if (body == null) return FlaggedStatus.UNKNOWN;
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) return FlaggedStatus.UNKNOWN;
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) return FlaggedStatus.UNKNOWN;
        String content = String.valueOf(message.get("content")).trim().toUpperCase();
        if (content.contains("SUSPICIOUS")) return FlaggedStatus.SUSPICIOUS;
        if (content.contains("VERIFIED"))   return FlaggedStatus.VERIFIED;
        return FlaggedStatus.UNKNOWN;
    }
}
