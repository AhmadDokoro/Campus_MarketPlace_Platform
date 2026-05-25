package com.ahsmart.campusmarket.service.embedding;

import com.ahsmart.campusmarket.helper.OpenAiConfig;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final String EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings";
    private static final String EMBEDDING_MODEL = "text-embedding-3-small";

    private final OpenAiConfig openAiConfig;
    private final ProductRepository productRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmbeddingServiceImpl(OpenAiConfig openAiConfig, ProductRepository productRepository) {
        this.openAiConfig = openAiConfig;
        this.productRepository = productRepository;
    }

    @Override
    public List<Double> generateEmbedding(String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", EMBEDDING_MODEL);
        body.put("input", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiConfig.getApiKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> response = restTemplate.postForEntity(EMBEDDINGS_URL, request, Map.class);

        return parseEmbedding(response.getBody());
    }

    @Override
    public String toJson(List<Double> embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize embedding", e);
        }
    }

    @Override
    public List<Double> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize embedding", e);
        }
    }

    @Override
    public int backfillMissingEmbeddings() {
        List<Product> products = productRepository.findAllWithoutEmbedding();
        log.info("Found {} products without embeddings", products.size());
        int updated = 0;
        for (Product product : products) {
            try {
                String text = buildEmbeddingText(product);
                List<Double> vector = generateEmbedding(text);
                product.setEmbedding(toJson(vector));
                productRepository.save(product);
                updated++;
                log.info("Generated embedding for product {} (\"{}\")", product.getProductId(), product.getTitle());
            } catch (Exception e) {
                log.warn("Failed to generate embedding for product {}: {}", product.getProductId(), e.getMessage());
            }
        }
        log.info("Embedding backfill complete: {}/{} products updated", updated, products.size());
        return updated;
    }

    private String buildEmbeddingText(Product product) {
        StringBuilder sb = new StringBuilder(product.getTitle());
        if (product.getCategory() != null && product.getCategory().getCategoryName() != null) {
            sb.append(" ").append(product.getCategory().getCategoryName());
        }
        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            sb.append(" ").append(product.getDescription());
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private List<Double> parseEmbedding(Map<?, ?> responseBody) {
        if (responseBody == null) {
            throw new RuntimeException("Empty response from OpenAI embeddings API");
        }
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("No embedding data in OpenAI response");
        }
        List<Number> rawEmbedding = (List<Number>) data.get(0).get("embedding");
        return rawEmbedding.stream().map(Number::doubleValue).toList();
    }
}
