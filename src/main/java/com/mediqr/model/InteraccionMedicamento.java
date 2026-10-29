package com.mediqr.model;

import jakarta.persistence.*;

@Entity
@Table(name = "interaccion_medicamento")
public class InteraccionMedicamento {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicamento_a", nullable = false)
    private String medicamentoA;

    @Column(name = "medicamento_b", nullable = false)
    private String medicamentoB;

    @Column(nullable = false, length = 10)
    private String gravedad;

    @Column(nullable = false, length = 300)
    private String mensaje;

    public InteraccionMedicamento() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMedicamentoA() { return medicamentoA; }
    public void setMedicamentoA(String medicamentoA) { this.medicamentoA = medicamentoA; }

    public String getMedicamentoB() { return medicamentoB; }
    public void setMedicamentoB(String medicamentoB) { this.medicamentoB = medicamentoB; }

    public String getGravedad() { return gravedad; }
    public void setGravedad(String gravedad) { this.gravedad = gravedad; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

}
