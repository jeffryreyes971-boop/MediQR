package com.mediqr.service;

import com.mediqr.model.Perfil;
import com.mediqr.repository.PerfilRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PerfilService {

    private final PerfilRepository perfiles;

    public PerfilService(PerfilRepository perfiles) { this.perfiles = perfiles; }

    /** Devuelve el perfil solo si pertenece al usuario que inició sesión; si no, error 403. */
    public Perfil obtenerSiEsDueno(Long perfilId, String correo) {
        Perfil p = perfiles.findById(perfilId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!p.getUsuario().getCorreo().equals(correo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return p;
    }
}
