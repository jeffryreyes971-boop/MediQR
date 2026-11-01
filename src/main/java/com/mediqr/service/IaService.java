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
            log.error("GEMINI_API_KEY no está presente en las variables de entorno.");
            return "No se ha configurado la clave de API de Gemini en el servidor.";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=" + apiKey;

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("contents", List.of(
            Map.of("role", "user", "parts", List.of(
                Map.of("text", instrucciones + "\n\nDATOS CLINICOS DEL PACIENTE:\n" + datos)
            ))
        ));

        try {
            log.info("Solicitando resumen clínico a Gemini (gemini-3.8-flash)...");

            Map<?, ?> resp = client.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(Map.class);

            if (resp == null) {
                log.warn("Respuesta nula recibida desde Gemini API.");
                return "No se recibió respuesta del servicio de IA.";
            }

            List<?> candidates = (List<?>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                log.warn("La respuesta de Gemini no contiene candidatos.");
                return "No se pudo generar el resumen clínico con los datos actuales.";
            }

            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) candidate.get("content");
            if (content == null) {
                return "Respuesta clínica incompleta.";
            }

            List<?> parts = (List<?>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                return "Respuesta vacía del modelo.";
            }

            Map<?, ?> part = (Map<?, ?>) parts.get(0);
            String texto = (String) part.get("text");

            log.info("Resumen clínico generado exitosamente.");
            return texto;

        } catch (HttpClientErrorException e) {
            log.error("Error HTTP ({}) al consultar Gemini: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Error en comunicación con la IA (" + e.getStatusCode() + ").";
        } catch (Exception e) {
            log.error("Error procesando respuesta de Gemini: {}", e.getMessage(), e);
            return "Error interno al procesar el resumen. Revisa los logs de Render.";
        }
    }
}
