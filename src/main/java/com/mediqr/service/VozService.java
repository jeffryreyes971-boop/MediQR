package com.mediqr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediqr.dto.RecetaDto;
import org.springframework.stereotype.Service;

@Service
public class VozService {

    private static final String INSTRUCCIONES = """
        Extrae los datos de una receta médica de la frase del usuario.
        Responde SOLO con un objeto JSON válido con estas claves exactas:
        - medicamento (String)
        - dosis (String)
        - frecuenciaHoras (Integer)
        - duracionDias (Integer)
        - medico (String)
        
        Usa null si un dato no aparece en la frase.
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
