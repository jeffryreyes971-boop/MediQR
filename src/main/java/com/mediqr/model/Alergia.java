package com.mediqr.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alergia")
public class Alergia {

    public enum Gravedad { LEVE, MEDIA, GRAVE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;

    @Column(nullable = false, length = 100)
    private String sustancia;

    @Column(length = 200)
    private String reaccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gravedad gravedad = Gravedad.MEDIA;

    public Alergia() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public String getSustancia() { return sustancia; }
    public void setSustancia(String sustancia) { this.sustancia = sustancia; }

    public String getReaccion() { return reaccion; }
    public void setReaccion(String reaccion) { this.reaccion = reaccion; }

    public Gravedad getGravedad() { return gravedad; }
    public void setGravedad(Gravedad gravedad) { this.gravedad = gravedad; }

}
