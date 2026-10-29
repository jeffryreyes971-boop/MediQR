package com.mediqr.service;

import com.mediqr.model.Perfil;
import com.mediqr.model.TokenQr;
import com.mediqr.repository.TokenQrRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenQrService {

    private final TokenQrRepository repo;

    @Value("${mediqr.qr.minutos-vigencia:10}")
    private int minutos;

    public TokenQrService(TokenQrRepository repo) { this.repo = repo; }

    @Transactional
    public TokenQr generarConsulta(Perfil perfil) {
        TokenQr t = new TokenQr();
        t.setPerfil(perfil);
        t.setCodigo(UUID.randomUUID().toString());
        t.setExpiraEn(LocalDateTime.now().plusMinutes(minutos));
        return repo.save(t);
    }

    /** Intenta usar el QR. Si ya se usó, venció o no existe, devuelve vacío. */
    @Transactional
    public Optional<TokenQr> consumir(String codigo) {
        int filas = repo.marcarUsado(codigo, LocalDateTime.now());
        return filas == 1 ? repo.findByCodigo(codigo) : Optional.empty();
    }
}
