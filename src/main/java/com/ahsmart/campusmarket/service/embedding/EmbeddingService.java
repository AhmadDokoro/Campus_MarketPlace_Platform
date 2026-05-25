package com.ahsmart.campusmarket.service.embedding;

import java.util.List;

public interface EmbeddingService {

    List<Double> generateEmbedding(String text);

    String toJson(List<Double> embedding);

    List<Double> fromJson(String json);

    int backfillMissingEmbeddings();
}
