package com.mediqr.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediqr.dto.RecetaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VozService {

    private static final Logger log = LoggerFactory.getLogger(VozService.class);

    private static final String INSTRUCCIONES = """
        Eres un asistente médico experto en extraer datos de recetas dictadas por voz.
        Analiza el texto ingresado y extrae la información requerida.

        Devuelve ÚNICAMENTE un objeto JSON sin formato markdown con los siguientes atributos:
        - "medicamento": (String) El nombre del medicamento (ej: "ranitidina", "amoxicilina").
        - "dosis": (String) Cantidad y unidad (ej: "500 mg", "10 ml").
        - "frecuenciaHoras": (Integer) Solo el número entero de horas entre tomas (ej: 8).
        - "duracionDias": (Integer) Solo el número entero de días de tratamiento (ej: 7).
        - "medico": (String) Nombre del médico (ej: "Dr. García").

        REGLAS ESTRICTAS:
        1. Responde SOLAMENTE con el JSON.
        2. No incluyas bloques ```json ni texto adicional.
        3. Si un dato no aparece en el texto, asígnale null.
        """;

    private final IaService ia;
    private final ObjectMapper mapper;

    public VozService(IaService ia, ObjectMapper mapper) {
        this.ia = ia;
        // Configuración tolerante para evitar fallos en la deserialización de Jackson
        this.mapper = mapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public RecetaDto interpretar(String texto) {
        try {
            String respuestaRaw = ia.preguntar(INSTRUCCIONES, texto);
            log.info("Respuesta cruda de IA: {}", respuestaRaw);

            if (respuestaRaw == null || respuestaRaw.isBlank()) {
                return new RecetaDto(null, null, null, null, null);
            }

            // Extraer únicamente el bloque JSON {...} omitiendo marcas de markdown o texto
            String json = extraerJson(respuestaRaw);
            log.info("JSON limpio para parsear: {}", json);

            if (json.isBlank() || json.equals("{}")) {
                return new RecetaDto(null, null, null, null, null);
            }

            return mapper.readValue(json, RecetaDto.class);

        } catch (Exception e) {
            log.error("Fallo al interpretar la receta con IA: {}", e.getMessage(), e);
            return new RecetaDto(null, null, null, null, null);
        }
    }

    private String extraerJson(String texto) {
        Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(texto);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return texto.replaceAll("```json", "").replaceAll("```", "").trim();
    }
}
