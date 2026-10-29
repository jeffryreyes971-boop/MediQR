package com.mediqr.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "enfermedad")
public class Enfermedad {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;

    @Column(nullable = false, length = 120)
    private String nombre;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;

    private boolean cronica;

    @Column(length = 300)
    private String notas;

    public Enfermedad() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }

    public boolean isCronica() { return cronica; }
    public void setCronica(boolean cronica) { this.cronica = cronica; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

}
