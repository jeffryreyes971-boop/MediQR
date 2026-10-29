package com.mediqr.controller;

import com.mediqr.model.Examen;
import com.mediqr.model.Perfil;
import com.mediqr.model.ResultadoLaboratorio;
import com.mediqr.repository.ExamenRepository;
import com.mediqr.repository.ResultadoLaboratorioRepository;
import com.mediqr.service.PerfilService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfiles/{perfilId}/examenes")
public class ExamenController {

    private final PerfilService perfilService;
    private final ExamenRepository examenes;
    private final ResultadoLaboratorioRepository resultados;

    public ExamenController(PerfilService perfilService, ExamenRepository examenes,
                            ResultadoLaboratorioRepository resultados) {
        this.perfilService = perfilService;
        this.examenes = examenes;
        this.resultados = resultados;
    }

    @GetMapping
    public String listar(@PathVariable Long perfilId, Principal pr, Model model) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        model.addAttribute("perfil", perfil);
        model.addAttribute("examenes", examenes.findByPerfilIdOrderByFechaDesc(perfilId));
        model.addAttribute("resultados", resultados.findByPerfilIdOrderByFechaDesc(perfilId));
        model.addAttribute("nuevo", new Examen());
        model.addAttribute("nuevoResultado", new ResultadoLaboratorio());
        return "examenes/lista";
    }

    @PostMapping
    public String guardarExamen(@PathVariable Long perfilId, @ModelAttribute("nuevo") Examen form, Principal pr) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        form.setId(null);
        form.setPerfil(perfil);
        examenes.save(form);
        return "redirect:/perfiles/" + perfilId + "/examenes";
    }

    @PostMapping("/resultados")
    public String guardarResultado(@PathVariable Long perfilId,
                                   @ModelAttribute("nuevoResultado") ResultadoLaboratorio form, Principal pr) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        form.setId(null);
        form.setPerfil(perfil);
        form.setExamen(null);
        if (form.getAnalito() == null || form.getAnalito().isBlank()
                || form.getValor() == null || form.getFecha() == null) {
            return "redirect:/perfiles/" + perfilId + "/examenes";   // faltan datos obligatorios
        }
        form.setAnalito(form.getAnalito().trim().toUpperCase());   // CREATININA, GLUCOSA...
        resultados.save(form);
        return "redirect:/perfiles/" + perfilId + "/examenes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarExamen(@PathVariable Long perfilId, @PathVariable Long id, Principal pr) {
        perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        examenes.findById(id)
            .filter(x -> x.getPerfil().getId().equals(perfilId))
            .ifPresent(examenes::delete);
        return "redirect:/perfiles/" + perfilId + "/examenes";
    }

    @PostMapping("/resultados/{id}/eliminar")
    public String eliminarResultado(@PathVariable Long perfilId, @PathVariable Long id, Principal pr) {
        perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        resultados.findById(id)
            .filter(x -> x.getPerfil().getId().equals(perfilId))
            .ifPresent(resultados::delete);
        return "redirect:/perfiles/" + perfilId + "/examenes";
    }
}
