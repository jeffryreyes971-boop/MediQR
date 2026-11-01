package com.mediqr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import java.util.List;

@Service
public class IaService {

    private static final Logger log = LoggerFactory.getLogger(IaService.class);
    private final RestClient client = RestClient.create();

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    // Estrategia DTO interna para mapeo exacto y seguro de JSON de Gemini
    public record Part(String text) {}
    public record Content(List<Part> parts, String role) {}
    public record Candidate(Content content) {}
    public record GeminiResponse(List<Candidate> candidates) {}

    public record TextPart(String text) {}
    public record ContentRequest(String role, List<TextPart> parts) {}
    public record GeminiRequest(List<ContentRequest> contents) {}

    public String preguntar(String instrucciones, String datos) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("GEMINI_API_KEY no está presente en el entorno de Render.");
            return "No se ha configurado la clave de API de Gemini.";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=" + apiKey.trim();

        GeminiRequest payload = new GeminiRequest(List.of(
            new ContentRequest("user", List.of(
                new TextPart(instrucciones + "\n\nDATOS CLINICOS DEL PACIENTE:\n" + datos)
            ))
        ));

        try {
            log.info("Enviando petición a Gemini (gemini-3.8-flash)...");

            GeminiResponse resp = client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(GeminiResponse.class);

            if (resp != null && resp.candidates() != null && !resp.candidates().isEmpty()) {
                Candidate firstCandidate = resp.candidates().get(0);
                if (firstCandidate.content() != null && firstCandidate.content().parts() != null && !firstCandidate.content().parts().isEmpty()) {
                    String texto = firstCandidate.content().parts().get(0).text();
                    log.info("Resumen médico generado con éxito.");
                    return texto;
                }
            }

            log.warn("Respuesta recibida de Gemini pero sin estructura de texto utilizable.");
            return "No se pudo extraer la información del resumen médico.";

        } catch (HttpClientErrorException e) {
            log.error("Error HTTP ({}) al consultar Gemini API: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Error al comunicar con la IA (" + e.getStatusCode() + " " + e.getStatusText() + "). Revisa los logs.";
        } catch (Exception e) {
            log.error("Excepción en IaService al procesar la solicitud: {}", e.getMessage(), e);
            return "Error al generar el resumen médico. Intenta de nuevo más tarde.";
        }
    }
}
