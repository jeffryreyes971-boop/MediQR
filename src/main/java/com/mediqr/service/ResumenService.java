package com.mediqr.service;

import com.mediqr.model.Perfil;
import com.mediqr.repository.*;
import java.time.LocalDate;
import java.time.Period;
import org.springframework.stereotype.Service;

@Service
public class ResumenService {

    private static final String INSTRUCCIONES = """
        Eres un asistente que prepara resúmenes clínicos de UNA página para médicos.
        Usa solo los datos recibidos; no inventes nada. Si falta información, indícalo.
        Estructura: 1) Alergias (primero, destacadas) 2) Medicamentos activos
        3) Enfermedades 4) Vacunas 5) Valores de laboratorio recientes.
        Usa frases cortas. Escribe en español. Termina con:
        "Resumen generado por IA; verificar con los documentos originales."
        """;

    private final PerfilRepository perfiles;
    private final AlergiaRepository alergias;
    private final RecetaRepository recetas;
    private final EnfermedadRepository enfermedades;
    private final VacunaRepository vacunas;
    private final ResultadoLaboratorioRepository laboratorio;
    private final IaService ia;

    public ResumenService(PerfilRepository perfiles, AlergiaRepository alergias,
                          RecetaRepository recetas, EnfermedadRepository enfermedades,
                          VacunaRepository vacunas, ResultadoLaboratorioRepository laboratorio,
                          IaService ia) {
        this.perfiles = perfiles;
        this.alergias = alergias;
        this.recetas = recetas;
        this.enfermedades = enfermedades;
        this.vacunas = vacunas;
        this.laboratorio = laboratorio;
        this.ia = ia;
    }

    public String resumenParaDoctor(Long perfilId) {
        Perfil p = perfiles.findById(perfilId).orElseThrow();
        StringBuilder sb = new StringBuilder();

        // PRIVACIDAD: no se envía nombre, correo ni teléfono; solo datos clínicos
        if (p.getFechaNacimiento() != null) {
            sb.append("Edad: ").append(Period.between(p.getFechaNacimiento(), LocalDate.now()).getYears()).append("\n");
        }
        sb.append("Tipo de sangre: ").append(p.getTipoSangre() == null ? "no registrado" : p.getTipoSangre());

        sb.append("\n\nALERGIAS:\n");
        alergias.findByPerfilId(perfilId).forEach(a ->
            sb.append("- ").append(a.getSustancia()).append(" (").append(a.getReaccion())
              .append(", ").append(a.getGravedad()).append(")\n"));

        sb.append("\nMEDICAMENTOS ACTIVOS:\n");
        recetas.findByPerfilIdAndActivaTrue(perfilId).forEach(r ->
            sb.append("- ").append(r.getMedicamento()).append(" ").append(r.getDosis())
              .append(" cada ").append(r.getFrecuenciaHoras()).append(" h\n"));

        sb.append("\nENFERMEDADES:\n");
        enfermedades.findByPerfilId(perfilId).forEach(e ->
            sb.append("- ").append(e.getNombre()).append(e.isCronica() ? " (crónica)" : "").append("\n"));

        sb.append("\nVACUNAS:\n");
        vacunas.findByPerfilIdOrderByFechaAplicacionDesc(perfilId).forEach(v ->
            sb.append("- ").append(v.getNombre()).append(" (").append(v.getFechaAplicacion()).append(")\n"));

        sb.append("\nLABORATORIO:\n");
        laboratorio.findByPerfilIdOrderByFechaDesc(perfilId).stream().limit(10).forEach(r ->
            sb.append("- ").append(r.getAnalito()).append(": ").append(r.getValor())
              .append(" ").append(r.getUnidad() == null ? "" : r.getUnidad())
              .append(" (límite ").append(r.getRefMax()).append(", ").append(r.getFecha()).append(")\n"));

        return ia.preguntar(INSTRUCCIONES, sb.toString());
    }
}
