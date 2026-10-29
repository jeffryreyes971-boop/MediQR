package com.mediqr.controller;

import com.mediqr.model.Perfil;
import com.mediqr.model.Recordatorio;
import com.mediqr.repository.RecordatorioRepository;
import com.mediqr.service.PerfilService;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Recordatorios de vacunas, controles y medicamentos. Los envía RecordatorioJob por correo. */
@Controller
@RequestMapping("/perfiles/{perfilId}/recordatorios")
public class RecordatorioController {

    private static final Set<String> TIPOS = Set.of("VACUNA", "CONTROL", "MEDICAMENTO");

    private final PerfilService perfilService;
    private final RecordatorioRepository repo;

    public RecordatorioController(PerfilService perfilService, RecordatorioRepository repo) {
        this.perfilService = perfilService;
        this.repo = repo;
    }

    @GetMapping
    public String listar(@PathVariable Long perfilId, Principal pr, Model model) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        model.addAttribute("perfil", perfil);
        model.addAttribute("recordatorios", repo.findByPerfilIdOrderByFechaHoraAsc(perfilId));
        return "recordatorios/lista";
    }

    @PostMapping
    public String guardar(@PathVariable Long perfilId,
                          @RequestParam String titulo,
                          @RequestParam String tipo,
                          @RequestParam("fechaHora")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
                          Principal pr, RedirectAttributes flash) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());

        String t = titulo == null ? "" : titulo.trim();
        if (t.isEmpty()) {
            flash.addFlashAttribute("error", "Escribe un título para el recordatorio.");
            return "redirect:/perfiles/" + perfilId + "/recordatorios";
        }
        if (!TIPOS.contains(tipo)) tipo = "CONTROL";

        Recordatorio r = new Recordatorio();
        r.setPerfil(perfil);
        r.setTitulo(t.length() > 150 ? t.substring(0, 150) : t);
        r.setTipo(tipo);
        r.setFechaHora(fechaHora);
        repo.save(r);
        flash.addFlashAttribute("ok", "Recordatorio guardado.");
        return "redirect:/perfiles/" + perfilId + "/recordatorios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long perfilId, @PathVariable Long id, Principal pr) {
        perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        repo.findById(id)
            .filter(x -> x.getPerfil().getId().equals(perfilId))   // evita borrar datos ajenos
            .ifPresent(repo::delete);
        return "redirect:/perfiles/" + perfilId + "/recordatorios";
    }
}
