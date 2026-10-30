package com.mediqr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediqr.dto.RecetaDto;
import org.springframework.stereotype.Service;

@Service
public class VozService {

    private static final String INSTRUCCIONES = """
        Extrae los datos de una receta médica de la frase entregada por el usuario.
        Responde ÚNICAMENTE con un objeto JSON válido, sin texto explicativo adicional ni comillas invertidas.
        Las claves del JSON deben ser exactamente:
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
            String respuestaIa = ia.preguntar(INSTRUCCIONES, texto);
            
            // Limpieza robusta de etiquetas markdown o comillas invertidas
            String jsonLimpio = respuestaIa.replaceAll("```json", "")
                                           .replaceAll("```", "")
                                           .trim();

            return mapper.readValue(jsonLimpio, RecetaDto.class);
        } catch (Exception e) {
            // Si la IA no responde un JSON perfecto o falla, devuelve un objeto seguro en lugar de lanzar excepción
            return new RecetaDto(null, null, null, null, null);
        }
    }
}
