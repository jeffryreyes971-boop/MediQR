package com.mediqr.dto;

import jakarta.validation.constraints.*;

public class RecetaForm {

    @NotBlank(message = "Escribe el medicamento")
    private String medicamento;

    private String dosis;

    private Integer frecuenciaHoras;

    private Integer duracionDias;

    private String medico;

    private boolean critico;

    public RecetaForm() { }

    public String getMedicamento() { return medicamento; }
    public void setMedicamento(String medicamento) { this.medicamento = medicamento; }

    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }

    public Integer getFrecuenciaHoras() { return frecuenciaHoras; }
    public void setFrecuenciaHoras(Integer frecuenciaHoras) { this.frecuenciaHoras = frecuenciaHoras; }

    public Integer getDuracionDias() { return duracionDias; }
    public void setDuracionDias(Integer duracionDias) { this.duracionDias = duracionDias; }

    public String getMedico() { return medico; }
    public void setMedico(String medico) { this.medico = medico; }

    public boolean isCritico() { return critico; }
    public void setCritico(boolean critico) { this.critico = critico; }

}
