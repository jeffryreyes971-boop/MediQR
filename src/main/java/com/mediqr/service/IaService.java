package com.mediqr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IaService {

    private static final Logger log = LoggerFactory.getLogger(IaService.class);
    private final RestClient client = RestClient.create("https://generativelanguage.googleapis.com");

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    public String preguntar(String instrucciones, String datos) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY no está configurada.");
            return "{}";
        }

        // URL actualizada con el modelo estándar v1beta
        String url = "/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("contents", List.of(
            Map.of("role", "user", "parts", List.of(
                Map.of("text", instrucciones + "\n\nTexto a procesar:\n" + datos)
            ))
        ));

        Map<String, Object> config = new HashMap<>();
        config.put("response_mime_type", "application/json");
        cuerpo.put("generationConfig", config);

        try {
            Map<?, ?> resp = client.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(Map.class);

            if (resp == null) return "{}";

            List<?> candidates = (List<?>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "{}";

            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) candidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> part = (Map<?, ?>) parts.get(0);

            String textoResultado = (String) part.get("text");
            log.info("Respuesta directa de Gemini: {}", textoResultado);
            return textoResultado;

        } catch (Exception e) {
            log.error("Error al comunicarse con la API de Gemini: {}", e.getMessage(), e);
            return "{}";
        }
    }
}
