package com.mediqr.controller;

import com.mediqr.model.Perfil;
import com.mediqr.service.PerfilService;
import com.mediqr.service.ResumenService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ResumenController {

    private final PerfilService perfilService;
    private final ResumenService resumenService;

    public ResumenController(PerfilService perfilService, ResumenService resumenService) {
        this.perfilService = perfilService;
        this.resumenService = resumenService;
    }

    @GetMapping("/perfiles/{perfilId}/resumen")
    public String resumen(@PathVariable Long perfilId, Principal pr, Model model) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        model.addAttribute("perfil", perfil);
        try {
            model.addAttribute("resumen", resumenService.resumenParaDoctor(perfilId));
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "resumen";
    }
}
