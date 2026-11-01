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
            log.error("GEMINI_API_KEY no está presente en el entorno.");
            return "No se ha configurado la API Key de Gemini.";
        }

        // Endpoint usando gemini-1.5-flash
        String url = "/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("contents", List.of(
            Map.of("role", "user", "parts", List.of(
                Map.of("text", instrucciones + "\n\nDATOS DEL PACIENTE:\n" + datos)
            ))
        ));

        try {
            log.info("Generando resumen médico con Gemini para los datos recibidos...");
            
            Map<?, ?> resp = client.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(Map.class);

            if (resp == null) return "No se recibió respuesta del servicio de IA.";

            List<?> candidates = (List<?>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "No se pudo generar el resumen clínico.";
            }

            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) candidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> part = (Map<?, ?>) parts.get(0);

            String texto = (String) part.get("text");
            log.info("Resumen generado exitosamente.");
            return texto;

        } catch (Exception e) {
            log.error("Error al consultar Gemini API: {}", e.getMessage(), e);
            return "Error al generar el resumen médico. Intente de nuevo más tarde.";
        }
    }
}
