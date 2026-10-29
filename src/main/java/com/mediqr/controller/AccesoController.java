package com.mediqr.controller;

import com.mediqr.repository.AuditoriaRepository;
import com.mediqr.service.PerfilService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AccesoController {

    private final PerfilService perfilService;
    private final AuditoriaRepository auditoria;

    public AccesoController(PerfilService perfilService, AuditoriaRepository auditoria) {
        this.perfilService = perfilService;
        this.auditoria = auditoria;
    }

    /** "¿Quién vio mi información y cuándo?" */
    @GetMapping("/perfiles/{perfilId}/accesos")
    public String listar(@PathVariable Long perfilId, Principal pr, Model model) {
        model.addAttribute("perfil", perfilService.obtenerSiEsDueno(perfilId, pr.getName()));
        model.addAttribute("accesos", auditoria.findByPerfilIdOrderByFechaDesc(perfilId));
        return "accesos/lista";
    }
}
