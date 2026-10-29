package com.mediqr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediqr.dto.RecetaDto;
import org.springframework.stereotype.Service;

@Service
public class VozService {

    private static final String INSTRUCCIONES = """
        Extrae los datos de una receta médica de la frase del usuario.
        Responde SOLO con un objeto JSON, sin texto adicional ni comillas invertidas,
        con estas claves: medicamento, dosis, frecuenciaHoras (entero),
        duracionDias (entero), medico. Usa null si un dato no aparece en la frase.
        """;

    private final IaService ia;
    private final ObjectMapper mapper;

    public VozService(IaService ia, ObjectMapper mapper) {
        this.ia = ia;
        this.mapper = mapper;
    }

    public RecetaDto interpretar(String texto) throws Exception {
        String json = ia.preguntar(INSTRUCCIONES, texto)
                        .replaceAll("```json|```", "").trim();
        return mapper.readValue(json, RecetaDto.class);   // valida que sea JSON correcto
    }
}
