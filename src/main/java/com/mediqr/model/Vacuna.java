package com.mediqr.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "vacuna")
public class Vacuna {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "fecha_aplicacion", nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaAplicacion;

    @Column(name = "proxima_dosis")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate proximaDosis;

    public Vacuna() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDate fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }

    public LocalDate getProximaDosis() { return proximaDosis; }
    public void setProximaDosis(LocalDate proximaDosis) { this.proximaDosis = proximaDosis; }

}
