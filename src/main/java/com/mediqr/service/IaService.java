package com.mediqr.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class IaService {

    private final RestClient client = RestClient.create("https://api.anthropic.com");

    @Value("${anthropic.api.key:}")
    private String apiKey;

    @Value("${anthropic.model}")
    private String modelo;

    /** Envía instrucciones + datos a la IA y devuelve el texto de la respuesta. */
    public String preguntar(String instrucciones, String datos) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta configurar la clave ANTHROPIC_API_KEY");
        }
        Map<String, Object> cuerpo = Map.of(
            "model", modelo,
            "max_tokens", 1000,
            "system", instrucciones,
            "messages", List.of(Map.of("role", "user", "content", datos))
        );
        try {
            Map<?, ?> resp = client.post().uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(Map.class);

            List<?> contenido = (List<?>) resp.get("content");
            return (String) ((Map<?, ?>) contenido.get(0)).get("text");
        } catch (Exception e) {
            // Si falla internet o la API, la aplicación NO debe caerse
            throw new IllegalStateException("No se pudo consultar a la IA: " + e.getMessage());
        }
    }
}
