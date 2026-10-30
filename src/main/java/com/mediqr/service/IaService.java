package com.mediqr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;

@Service
public class IaService {

    private final RestClient client = RestClient.create("https://generativelanguage.googleapis.com");

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    public String preguntar(String instrucciones, String datos) {
        if (apiKey == null || apiKey.isBlank()) {
            return "{}";
        }

        String url = "/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> cuerpo = Map.of(
            "contents", List.of(
                Map.of("role", "user", "parts", List.of(
                    Map.of("text", instrucciones + "\n\nTexto a procesar:\n" + datos)
                ))
            )
        );

        try {
            Map<?, ?> resp = client.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(Map.class);

            List<?> candidates = (List<?>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "{}";

            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) candidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> part = (Map<?, ?>) parts.get(0);

            return (String) part.get("text");
        } catch (Exception e) {
            return "{}";
        }
    }
}
