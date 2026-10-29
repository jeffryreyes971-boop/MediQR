package com.mediqr.controller;

import com.mediqr.model.Perfil;
import com.mediqr.model.TokenQr;
import com.mediqr.repository.*;
import com.mediqr.service.AuditoriaService;
import com.mediqr.service.TokenQrService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** SIN sesión: lo abre el médico o un rescatista al escanear el QR. */
@Controller
public class PublicoController {

    private final PerfilRepository perfiles;
    private final AlergiaRepository alergias;
    private final RecetaRepository recetas;
    private final EnfermedadRepository enfermedades;
    private final VacunaRepository vacunas;
    private final ContactoEmergenciaRepository contactos;
    private final TokenQrService tokenService;
    private final AuditoriaService auditoria;

    public PublicoController(PerfilRepository perfiles, AlergiaRepository alergias,
                             RecetaRepository recetas, EnfermedadRepository enfermedades,
                             VacunaRepository vacunas, ContactoEmergenciaRepository contactos,
                             TokenQrService tokenService, AuditoriaService auditoria) {
        this.perfiles = perfiles;
        this.alergias = alergias;
        this.recetas = recetas;
        this.enfermedades = enfermedades;
        this.vacunas = vacunas;
        this.contactos = contactos;
        this.tokenService = tokenService;
        this.auditoria = auditoria;
    }

    /** QR de emergencia: SOLO lo vital. */
    @GetMapping("/emergencia/{codigo}")
    public String emergencia(@PathVariable String codigo, HttpServletRequest req, Model model) {
        Optional<Perfil> op = perfiles.findByCodigoEmergencia(codigo);
        if (op.isEmpty()) return "qr/invalido";
        Perfil p = op.get();

        auditoria.registrar(p, "EMERGENCIA", req);
        model.addAttribute("perfil", p);
        model.addAttribute("alergias", alergias.findByPerfilId(p.getId()));
        model.addAttribute("criticos", recetas.findByPerfilIdAndActivaTrueAndCriticoTrue(p.getId()));
        model.addAttribute("contactos", contactos.findByPerfilId(p.getId()));
        return "qr/emergencia";
    }

    /** QR de consulta: resumen completo, válido una sola vez y por pocos minutos. */
    @GetMapping("/consulta/{codigo}")
    public String consulta(@PathVariable String codigo, HttpServletRequest req, Model model) {
        Optional<TokenQr> token = tokenService.consumir(codigo);
        if (token.isEmpty()) return "qr/invalido";      // usado, vencido o inexistente
        Perfil p = token.get().getPerfil();

        auditoria.registrar(p, "CONSULTA", req);
        model.addAttribute("perfil", p);
        model.addAttribute("alergias", alergias.findByPerfilId(p.getId()));
        model.addAttribute("recetas", recetas.findByPerfilIdAndActivaTrue(p.getId()));
        model.addAttribute("enfermedades", enfermedades.findByPerfilId(p.getId()));
        model.addAttribute("vacunas", vacunas.findByPerfilIdOrderByFechaAplicacionDesc(p.getId()));
        return "qr/consulta-medico";
    }
}
