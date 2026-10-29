package com.mediqr.controller;

import com.mediqr.model.Vacuna;
import com.mediqr.model.Perfil;
import com.mediqr.repository.VacunaRepository;
import com.mediqr.service.PerfilService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfiles/{perfilId}/vacunas")
public class VacunaController {

    private final PerfilService perfilService;
    private final VacunaRepository repo;

    public VacunaController(PerfilService perfilService, VacunaRepository repo) {
        this.perfilService = perfilService;
        this.repo = repo;
    }

    @GetMapping
    public String listar(@PathVariable Long perfilId, Principal pr, Model model) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        model.addAttribute("perfil", perfil);
        model.addAttribute("vacunas", repo.findByPerfilIdOrderByFechaAplicacionDesc(perfilId));
        model.addAttribute("nueva", new Vacuna());
        return "vacunas/lista";
    }

    @PostMapping
    public String guardar(@PathVariable Long perfilId, @ModelAttribute("nueva") Vacuna form, Principal pr) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        form.setId(null);
        form.setPerfil(perfil);
        repo.save(form);
        return "redirect:/perfiles/" + perfilId + "/vacunas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long perfilId, @PathVariable Long id, Principal pr) {
        perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        repo.findById(id)
            .filter(x -> x.getPerfil().getId().equals(perfilId))
            .ifPresent(repo::delete);
        return "redirect:/perfiles/" + perfilId + "/vacunas";
    }
}
