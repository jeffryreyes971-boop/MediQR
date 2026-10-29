package com.mediqr.controller;

import com.mediqr.dto.RecetaDto;
import com.mediqr.model.Perfil;
import com.mediqr.service.CruceLabService;
import com.mediqr.service.InteraccionService;
import com.mediqr.service.PerfilService;
import com.mediqr.service.VozService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Lo llama el JavaScript de la pantalla de recetas (voz.js). */
@RestController
@RequestMapping("/api/perfiles/{perfilId}/voz")
public class VozController {

    public record Entrada(String texto) { }
    public record Salida(RecetaDto receta, List<String> alertas) { }

    private final PerfilService perfilService;
    private final VozService vozService;
    private final InteraccionService interacciones;
    private final CruceLabService cruceLab;

    public VozController(PerfilService perfilService, VozService vozService,
                         InteraccionService interacciones, CruceLabService cruceLab) {
        this.perfilService = perfilService;
        this.vozService = vozService;
        this.interacciones = interacciones;
        this.cruceLab = cruceLab;
    }

    /** La IA PROPONE la receta; no se guarda hasta que el usuario la confirme en el formulario. */
    @PostMapping
    public Salida interpretar(@PathVariable Long perfilId, @RequestBody Entrada entrada, Principal pr) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        try {
            RecetaDto dto = vozService.interpretar(entrada.texto());
            List<String> alertas = new ArrayList<>();
            if (dto.medicamento() != null) {
                alertas.addAll(interacciones.revisar(perfil, dto.medicamento()));
                alertas.addAll(cruceLab.evaluar(perfil, dto.medicamento()));
            }
            return new Salida(dto, alertas);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo interpretar la receta");
        }
    }
}
