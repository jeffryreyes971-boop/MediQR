package com.mediqr.controller;

import com.mediqr.model.ContactoEmergencia;
import com.mediqr.model.Perfil;
import com.mediqr.repository.ContactoEmergenciaRepository;
import com.mediqr.service.PerfilService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfiles/{perfilId}/contactos")
public class ContactoController {

    private final PerfilService perfilService;
    private final ContactoEmergenciaRepository repo;

    public ContactoController(PerfilService perfilService, ContactoEmergenciaRepository repo) {
        this.perfilService = perfilService;
        this.repo = repo;
    }

    @GetMapping
    public String listar(@PathVariable Long perfilId, Principal pr, Model model) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        model.addAttribute("perfil", perfil);
        model.addAttribute("contactos", repo.findByPerfilId(perfilId));
        model.addAttribute("nuevo", new ContactoEmergencia());
        return "contactos/lista";
    }

    @PostMapping
    public String guardar(@PathVariable Long perfilId, @ModelAttribute("nuevo") ContactoEmergencia form, Principal pr) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        form.setId(null);
        form.setPerfil(perfil);
        repo.save(form);
        return "redirect:/perfiles/" + perfilId + "/contactos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long perfilId, @PathVariable Long id, Principal pr) {
        perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        repo.findById(id)
            .filter(x -> x.getPerfil().getId().equals(perfilId))
            .ifPresent(repo::delete);
        return "redirect:/perfiles/" + perfilId + "/contactos";
    }
}
