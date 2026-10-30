package com.mediqr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediqr.dto.RecetaDto;
import org.springframework.stereotype.Service;

@Service
public class VozService {

    private static final String INSTRUCCIONES = """
        Eres un asistente médico experto en extraer datos de recetas dictadas por voz.
        Analiza el texto y devuelve ÚNICAMENTE un objeto JSON estricto con los siguientes campos:

        - "medicamento": (String) El nombre del medicamento (ej: "ranitidina", "ibuprofeno", "paracetamol").
        - "dosis": (String) La cantidad y unidad (ej: "500 mg", "10 ml", "1 pastilla").
        - "frecuenciaHoras": (Integer) Número entero de horas entre tomas (ej: si dice "cada 8 horas" pones 8).
        - "duracionDias": (Integer) Número entero de días del tratamiento (ej: si dice "por 7 días" pones 7).
        - "medico": (String) Nombre del médico (ej: "Dr. García", "Doctora Pérez").

        Ejemplo de entrada: "este medicamento ranitidina dosis 500 mg cada 8 horas por 7 días médico doctor García"
        Ejemplo de salida JSON:
        {
          "medicamento": "ranitidina",
          "dosis": "500 mg",
          "frecuenciaHoras": 8,
          "duracionDias": 7,
          "medico": "Dr. García"
        }

        Si no encuentras un dato específico en el texto, coloca null en ese campo.
        No incluyas texto antes o después del JSON.
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
            
            // Limpieza estricta de marcadores de bloque de código
            String json = respuestaRaw.replaceAll("```json", "")
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
