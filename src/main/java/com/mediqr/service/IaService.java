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
    private final RestClient client = RestClient.create();

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    public String preguntar(String instrucciones, String datos) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("GEMINI_API_KEY no está configurada.");
            return "No se ha configurado la clave de API de Gemini.";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=" + apiKey.trim();

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("contents", List.of(
            Map.of("role", "user", "parts", List.of(
                Map.of("text", instrucciones + "\n\nDATOS CLINICOS DEL PACIENTE:\n" + datos)
            ))
        ));

        try {
            log.info("Enviando consulta a Gemini (gemini-3.8-flash)...");

            Map<?, ?> resp = client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(Map.class);

            if (resp == null) return "No se recibió respuesta del servicio de IA.";

            // Extraer candidates dinámicamente
            Object candidatesObj = resp.get("candidates");
            if (candidatesObj instanceof List<?> candidates && !candidates.isEmpty()) {
                Object candidateObj = candidates.get(0);
                if (candidateObj instanceof Map<?, ?> candidate) {
                    Object contentObj = candidate.get("content");
                    if (contentObj instanceof Map<?, ?> content) {
                        Object partsObj = content.get("parts");
                        if (partsObj instanceof List<?> parts && !parts.isEmpty()) {
                            Object partObj = parts.get(0);
                            if (partObj instanceof Map<?, ?> part) {
                                String texto = (String) part.get("text");
                                if (texto != null && !texto.isBlank()) {
                                    log.info("Resumen clínico generado exitosamente por Gemini.");
                                    return texto;
                                }
                            }
                        }
                    }
                }
            }

            log.warn("Respuesta de Gemini recibida pero con estructura no reconocida: {}", resp);
            return "No se pudo formatear el resumen recibido de la IA.";

        } catch (HttpClientErrorException e) {
            log.error("Error HTTP ({}) al consultar Gemini API: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Error al comunicar con la IA (" + e.getStatusCode() + " " + e.getStatusText() + ").";
        } catch (Exception e) {
            log.error("Excepción al procesar respuesta de Gemini: {}", e.getMessage(), e);
            return "Error al procesar el resumen clínico.";
        }
    }
}
