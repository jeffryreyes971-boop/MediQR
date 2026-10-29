package com.mediqr.service;

import com.mediqr.model.MedicamentoCatalogo;
import com.mediqr.model.Perfil;
import com.mediqr.repository.MedicamentoCatalogoRepository;
import com.mediqr.repository.ResultadoLaboratorioRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CruceLabService {

    private final MedicamentoCatalogoRepository catalogo;
    private final ResultadoLaboratorioRepository laboratorio;

    public CruceLabService(MedicamentoCatalogoRepository catalogo,
                           ResultadoLaboratorioRepository laboratorio) {
        this.catalogo = catalogo;
        this.laboratorio = laboratorio;
    }

    /** Si el fármaco se elimina por el riñón y la creatinina está alta, avisa. */
    public List<String> evaluar(Perfil perfil, String medicamento) {
        boolean renal = catalogo.findByNombreIgnoreCase(medicamento)
                                .map(MedicamentoCatalogo::isEliminacionRenal)
                                .orElse(false);
        if (!renal) return List.of();

        return laboratorio
            .findFirstByPerfilIdAndAnalitoIgnoreCaseOrderByFechaDesc(perfil.getId(), "CREATININA")
            .filter(r -> r.getRefMax() != null && r.getValor().compareTo(r.getRefMax()) > 0)
            .map(r -> List.of("Atención: " + medicamento + " requiere ajuste de dosis según tu último examen. "
                    + "Tu creatinina (" + r.getFecha() + ") fue " + r.getValor()
                    + (r.getUnidad() == null ? "" : " " + r.getUnidad())
                    + ", por encima del límite normal."))
            .orElse(List.of());
    }
}
