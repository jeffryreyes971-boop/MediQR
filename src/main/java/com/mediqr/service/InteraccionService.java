package com.mediqr.service;

import com.mediqr.model.*;
import com.mediqr.repository.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class InteraccionService {

    private final AlergiaRepository alergias;
    private final RecetaRepository recetas;
    private final InteraccionRepository interacciones;

    public InteraccionService(AlergiaRepository alergias, RecetaRepository recetas,
                              InteraccionRepository interacciones) {
        this.alergias = alergias;
        this.recetas = recetas;
        this.interacciones = interacciones;
    }

    /** Revisa un medicamento nuevo contra las alergias y recetas activas del perfil. */
    public List<String> revisar(Perfil perfil, String medicamentoNuevo) {
        List<String> alertas = new ArrayList<>();
        String nuevo = medicamentoNuevo.toLowerCase();

        for (Alergia a : alergias.findByPerfilId(perfil.getId())) {
            if (nuevo.contains(a.getSustancia().toLowerCase())) {
                alertas.add("ALERGIA: el paciente es alérgico a " + a.getSustancia() + ".");
            }
        }
        for (Receta r : recetas.findByPerfilIdAndActivaTrue(perfil.getId())) {
            for (InteraccionMedicamento i : interacciones.buscarPar(medicamentoNuevo, r.getMedicamento())) {
                alertas.add("INTERACCIÓN con " + r.getMedicamento() + " (" + i.getGravedad()
                            + "): " + i.getMensaje());
            }
        }
        return alertas;
    }
}
