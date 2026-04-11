package org.tutorbooking.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Primary

public class CustomGeminiEmbeddingModel implements EmbeddingModel {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String url = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=";

    @Override
    public float[] embed(Document document) {
        return embed(document.getText());
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[0];
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        
        parts.add(Map.of("text", text));
        content.put("parts", parts);
        body.put("content", content);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url + apiKey, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("embedding")) {
                Map<String, Object> embeddingNode = (Map<String, Object>) responseBody.get("embedding");
                List<Double> values = (List<Double>) embeddingNode.get("values");
                float[] result = new float[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    result[i] = values.get(i).floatValue();
                }
                return result;
            }
        } catch (Exception e) {
            System.err.println("Gemini Embedding Error: " + e.getMessage());
        }
        return new float[0];
    }

    // Removed invalid embedList method

    @Override
    public EmbeddingResponse call(org.springframework.ai.embedding.EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        List<String> texts = request.getInstructions();
        for (int i = 0; i < texts.size(); i++) {
            float[] floats = embed(texts.get(i));
            embeddings.add(new Embedding(floats, i));
        }
        return new EmbeddingResponse(embeddings, new EmbeddingResponseMetadata());
    }
    
    @Override
    public int dimensions() {
        return 768; // text-embedding-004 returns 768 dimensions
    }
}
