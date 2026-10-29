package com.mediqr.service;

import com.mediqr.model.AuditoriaAcceso;
import com.mediqr.model.Perfil;
import com.mediqr.repository.AuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    private final AuditoriaRepository repo;

    public AuditoriaService(AuditoriaRepository repo) { this.repo = repo; }

    public void registrar(Perfil perfil, String tipo, HttpServletRequest req) {
        AuditoriaAcceso a = new AuditoriaAcceso();
        a.setPerfil(perfil);
        a.setTipo(tipo);
        a.setIp(req.getRemoteAddr());
        String ua = req.getHeader("User-Agent");
        a.setUserAgent(ua == null ? null : ua.substring(0, Math.min(ua.length(), 250)));
        repo.save(a);
    }
}
