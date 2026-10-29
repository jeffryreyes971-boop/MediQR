package com.mediqr.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "resultado_laboratorio")
public class ResultadoLaboratorio {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;

    @ManyToOne
    @JoinColumn(name = "examen_id")
    private Examen examen;

    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @Column(nullable = false, length = 60)
    private String analito;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(length = 20)
    private String unidad;

    @Column(name = "ref_max")
    private BigDecimal refMax;

    public ResultadoLaboratorio() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public Examen getExamen() { return examen; }
    public void setExamen(Examen examen) { this.examen = examen; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getAnalito() { return analito; }
    public void setAnalito(String analito) { this.analito = analito; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public BigDecimal getRefMax() { return refMax; }
    public void setRefMax(BigDecimal refMax) { this.refMax = refMax; }

}
