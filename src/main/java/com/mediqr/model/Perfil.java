package com.mediqr.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "perfil")
public class Perfil {

    public enum Parentesco { TITULAR, HIJO, ADULTO_MAYOR, OTRO }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Parentesco parentesco = Parentesco.TITULAR;

    @Column(name = "fecha_nacimiento")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @Column(name = "tipo_sangre", length = 5)
    private String tipoSangre;

    @Column(name = "codigo_emergencia", nullable = false, unique = true, length = 36)
    private String codigoEmergencia = UUID.randomUUID().toString();

    public Perfil() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Parentesco getParentesco() { return parentesco; }
    public void setParentesco(Parentesco parentesco) { this.parentesco = parentesco; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTipoSangre() { return tipoSangre; }
    public void setTipoSangre(String tipoSangre) { this.tipoSangre = tipoSangre; }

    public String getCodigoEmergencia() { return codigoEmergencia; }
    public void setCodigoEmergencia(String codigoEmergencia) { this.codigoEmergencia = codigoEmergencia; }

}
