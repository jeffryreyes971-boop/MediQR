package com.mediqr.controller;

import com.mediqr.dto.RecetaForm;
import com.mediqr.model.Perfil;
import com.mediqr.model.Receta;
import com.mediqr.repository.RecetaRepository;
import com.mediqr.service.CruceLabService;
import com.mediqr.service.InteraccionService;
import com.mediqr.service.PerfilService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfiles/{perfilId}/recetas")
public class RecetaController {

    private final PerfilService perfilService;
    private final RecetaRepository recetas;
    private final InteraccionService interacciones;
    private final CruceLabService cruceLab;

    public RecetaController(PerfilService perfilService, RecetaRepository recetas,
                            InteraccionService interacciones, CruceLabService cruceLab) {
        this.perfilService = perfilService;
        this.recetas = recetas;
        this.interacciones = interacciones;
        this.cruceLab = cruceLab;
    }

    @GetMapping
    public String listar(@PathVariable Long perfilId, Principal pr, Model model) {
        model.addAttribute("perfil", perfilService.obtenerSiEsDueno(perfilId, pr.getName()));
        model.addAttribute("recetas", recetas.findByPerfilIdOrderByFechaRecetaDesc(perfilId));
        return "recetas/lista";
    }

    @GetMapping("/nueva")
    public String form(@PathVariable Long perfilId, Principal pr, Model model) {
        model.addAttribute("perfil", perfilService.obtenerSiEsDueno(perfilId, pr.getName()));
        model.addAttribute("receta", new RecetaForm());
        return "recetas/form";
    }

    @PostMapping
    public String guardar(@PathVariable Long perfilId,
                          @Valid @ModelAttribute("receta") RecetaForm form,
                          BindingResult errores,
                          @RequestParam(defaultValue = "false") boolean confirmar,
                          Principal pr, Model model) {

        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        model.addAttribute("perfil", perfil);
        if (errores.hasErrors()) return "recetas/form";

        // Alertas ANTES de guardar: alergias, interacciones y cruce con laboratorio
        List<String> alertas = new ArrayList<>();
        alertas.addAll(interacciones.revisar(perfil, form.getMedicamento()));
        alertas.addAll(cruceLab.evaluar(perfil, form.getMedicamento()));

        if (!alertas.isEmpty() && !confirmar) {
            model.addAttribute("alertas", alertas);   // la vista muestra el aviso y el botón
            return "recetas/form";                    // "Guardar de todos modos"
        }

        Receta r = new Receta();
        r.setPerfil(perfil);
        r.setMedicamento(form.getMedicamento().trim());
        r.setDosis(form.getDosis());
        r.setFrecuenciaHoras(form.getFrecuenciaHoras());
        r.setDuracionDias(form.getDuracionDias());
        r.setMedico(form.getMedico());
        r.setCritico(form.isCritico());
        recetas.save(r);
        return "redirect:/perfiles/" + perfilId + "/recetas";
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long perfilId, @PathVariable Long id, Principal pr) {
        perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        recetas.findById(id)
               .filter(r -> r.getPerfil().getId().equals(perfilId))
               .ifPresent(r -> { r.setActiva(false); recetas.save(r); });
        return "redirect:/perfiles/" + perfilId + "/recetas";
    }
}
