package com.mediqr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Receta interpretada por la IA a partir de lo que dijo el usuario. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RecetaDto(String medicamento, String dosis, Integer frecuenciaHoras,
                        Integer duracionDias, String medico) { }
