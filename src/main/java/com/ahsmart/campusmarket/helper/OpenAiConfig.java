package com.ahsmart.campusmarket.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    // OpenAI chat completions endpoint.
    public static final String API_URL = "https://api.openai.com/v1/chat/completions";

    // Model used for fraud classification — cheap, fast, and accurate for single-label tasks.
    public static final String MODEL = "gpt-4o-mini";

    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}
