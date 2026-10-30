package com.mediqr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediqr.dto.RecetaDto;
import org.springframework.stereotype.Service;

@Service
public class VozService {

    private static final String INSTRUCCIONES = """
        Extrae los datos de una receta médica del texto entregado.
        Responde ÚNICAMENTE con un objeto JSON válido sin texto adicional.
        Usa exactamente estas claves:
        {
          "medicamento": "Nombre del medicamento",
          "dosis": "Dosis expresada como string, ej: 500 mg",
          "frecuenciaHoras": 8,
          "duracionDias": 7,
          "medico": "Nombre del médico o doctor"
        }
        Si no encuentras un dato, asigna null.
        """;

    private final IaService ia;
    private final ObjectMapper mapper;

    public VozService(IaService ia, ObjectMapper mapper) {
        this.ia = ia;
        this.mapper = mapper;
    }

    public RecetaDto interpretar(String texto) {
        try {
            String json = ia.preguntar(INSTRUCCIONES, texto)
                            .replaceAll("```json", "")
                            .replaceAll("```", "")
                            .trim();

            if (json.isBlank() || json.equals("{}")) {
                return new RecetaDto(null, null, null, null, null);
            }

            return mapper.readValue(json, RecetaDto.class);
        } catch (Exception e) {
            return new RecetaDto(null, null, null, null, null);
        }
    }
}
