package com.mediqr.service;

import com.fasterxml.jackson.databind.JsonNode;
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

        Devuelve ÚNICAMENTE un objeto JSON estricto sin formato markdown con los siguientes atributos:
        - "medicamento": (String) El nombre del medicamento (ej: "ranitidina", "amoxicilina").
        - "dosis": (String) Cantidad y unidad (ej: "500 mg", "10 ml").
        - "frecuenciaHoras": (Integer) Solo el número entero de horas entre tomas (ej: 8).
        - "duracionDias": (Integer) Solo el número entero de días de tratamiento (ej: 7).
        - "medico": (String) Nombre del médico (ej: "Dr. García").

        REGLAS ESTRICTAS:
        1. Responde SOLAMENTE con el objeto JSON.
        2. No incluyas bloques ```json ni texto adicional.
        3. Si un dato no aparece en el texto, asignale null.
        """;

    private final IaService ia;
    private final ObjectMapper mapper;

    public VozService(IaService ia, ObjectMapper mapper) {
        this.ia = ia;
        this.mapper = mapper;
    }

    public RecetaDto interpretar(String texto) {
        try {
            String respuestaRaw = ia.preguntar(INSTRUCCIONES, texto);
            log.info("Respuesta cruda de IA: {}", respuestaRaw);

            if (respuestaRaw == null || respuestaRaw.isBlank() || respuestaRaw.trim().equals("{}")) {
                return new RecetaDto(null, null, null, null, null);
            }

            String jsonStr = extraerJson(respuestaRaw);
            log.info("JSON extraído para procesar: {}", jsonStr);

            JsonNode root = mapper.readTree(jsonStr);

            String medicamento = obtenerTexto(root, "medicamento", "nombre", "nombreMedicamento");
            String dosis = obtenerTexto(root, "dosis", "cantidad");
            Integer frecuenciaHoras = obtenerEntero(root, "frecuenciaHoras", "frecuencia_horas", "frecuencia");
            Integer duracionDias = obtenerEntero(root, "duracionDias", "duracion_dias", "duracion");
            String medico = obtenerTexto(root, "medico", "doctor", "nombreMedico");

            RecetaDto resultado = new RecetaDto(medicamento, dosis, frecuenciaHoras, duracionDias, medico);
            log.info("RecetaDto construido exitosamente: {}", resultado);
            return resultado;

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

    private String obtenerTexto(JsonNode root, String... alias) {
        for (String key : alias) {
            if (root.hasNonNull(key)) {
                String val = root.get(key).asText().trim();
                if (!val.equalsIgnoreCase("null") && !val.isBlank()) {
                    return val;
                }
            }
        }
        return null;
    }

    private Integer obtenerEntero(JsonNode root, String... alias) {
        for (String key : alias) {
            if (root.hasNonNull(key)) {
                JsonNode node = root.get(key);
                if (node.isInt() || node.isNumber()) {
                    return node.asInt();
                }
                if (node.isTextual()) {
                    try {
                        String limpio = node.asText().replaceAll("[^0-9]", "");
                        if (!limpio.isBlank()) {
                            return Integer.parseInt(limpio);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return null;
    }
}
