package com.mediqr.controller;

import com.mediqr.model.Alergia;
import com.mediqr.model.Perfil;
import com.mediqr.repository.AlergiaRepository;
import com.mediqr.service.PerfilService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfiles/{perfilId}/alergias")
public class AlergiaController {

    private final PerfilService perfilService;
    private final AlergiaRepository alergias;

    public AlergiaController(PerfilService perfilService, AlergiaRepository alergias) {
        this.perfilService = perfilService;
        this.alergias = alergias;
    }

    @GetMapping
    public String listar(@PathVariable Long perfilId, Principal pr, Model model) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        model.addAttribute("perfil", perfil);
        model.addAttribute("alergias", alergias.findByPerfilId(perfilId));
        model.addAttribute("nueva", new Alergia());
        return "alergias/lista";
    }

    @PostMapping
    public String guardar(@PathVariable Long perfilId, @ModelAttribute("nueva") Alergia form, Principal pr) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        form.setId(null);          // nunca confiamos en el id que llega del formulario
        form.setPerfil(perfil);    // el perfil lo decide el servidor, no el formulario
        alergias.save(form);
        return "redirect:/perfiles/" + perfilId + "/alergias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long perfilId, @PathVariable Long id, Principal pr) {
        perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        alergias.findById(id)
                .filter(a -> a.getPerfil().getId().equals(perfilId))   // evita borrar datos ajenos
                .ifPresent(alergias::delete);
        return "redirect:/perfiles/" + perfilId + "/alergias";
    }
}
