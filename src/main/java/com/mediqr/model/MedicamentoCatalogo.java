package com.mediqr.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medicamento_catalogo")
public class MedicamentoCatalogo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(name = "eliminacion_renal")
    private boolean eliminacionRenal;

    public MedicamentoCatalogo() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isEliminacionRenal() { return eliminacionRenal; }
    public void setEliminacionRenal(boolean eliminacionRenal) { this.eliminacionRenal = eliminacionRenal; }

}
