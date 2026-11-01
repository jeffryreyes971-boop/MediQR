package com.mediqr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
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
            log.error("GEMINI_API_KEY no está presente en la configuración.");
            return "No se ha configurado la clave de API de Gemini.";
        }

        // Endpoint usando v1 directo y el nombre exacto retornado por el API: gemini-2.5-flash
        String url = "/v1/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("contents", List.of(
            Map.of("role", "user", "parts", List.of(
                Map.of("text", instrucciones + "\n\nDATOS CLINICOS DEL PACIENTE:\n" + datos)
            ))
        ));

        try {
            log.info("Enviando petición a Gemini (gemini-2.5-flash)...");

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
            log.info("Resumen generado con éxito.");
            return texto;

        } catch (HttpClientErrorException e) {
            log.error("Error HTTP al llamar a Gemini API (Status {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Error al comunicar con la IA (" + e.getStatusCode() + " " + e.getStatusText() + "). Revisa los logs.";
        } catch (Exception e) {
            log.error("Excepción al consultar Gemini API: {}", e.getMessage(), e);
            return "Error al generar el resumen médico. Intenta de nuevo más tarde.";
        }
    }
}
