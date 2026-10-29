package com.mediqr.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "receta")
public class Receta {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;

    @Column(nullable = false, length = 120)
    private String medicamento;

    @Column(length = 60)
    private String dosis;

    @Column(name = "frecuencia_horas")
    private Integer frecuenciaHoras;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    @Column(length = 100)
    private String medico;

    @Column(name = "fecha_receta", nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaReceta = LocalDate.now();

    private boolean critico;

    private boolean activa = true;

    public Receta() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

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

    public LocalDate getFechaReceta() { return fechaReceta; }
    public void setFechaReceta(LocalDate fechaReceta) { this.fechaReceta = fechaReceta; }

    public boolean isCritico() { return critico; }
    public void setCritico(boolean critico) { this.critico = critico; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

}
