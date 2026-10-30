package com.mediqr.controller;

import com.mediqr.model.Perfil;
import com.mediqr.model.TokenQr;
import com.mediqr.service.PerfilService;
import com.mediqr.service.QrImageService;
import com.mediqr.service.TokenQrService;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** Requiere sesión iniciada: lo usa el paciente. */
@Controller
@RequestMapping("/perfiles/{perfilId}/qr")
public class QrController {

    private final PerfilService perfilService;
    private final TokenQrService tokenService;
    private final QrImageService qrImage;

    @Value("${mediqr.base-url}")
    private String baseUrl;

    public QrController(PerfilService perfilService, TokenQrService tokenService, QrImageService qrImage) {
        this.perfilService = perfilService;
        this.tokenService = tokenService;
        this.qrImage = qrImage;
    }

    /** Pantalla con los dos QR: emergencia (permanente) y consulta (nuevo, con vencimiento). */
    @GetMapping
    public String pantalla(@PathVariable Long perfilId, Principal pr, Model model) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        TokenQr token = tokenService.generarConsulta(perfil);

        model.addAttribute("perfil", perfil);
        model.addAttribute("codigoConsulta", token.getCodigo());
        model.addAttribute("segundos", Duration.between(LocalDateTime.now(), token.getExpiraEn()).getSeconds());
        return "qr/pantalla";
    }

    @GetMapping(value = "/emergencia.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] pngEmergencia(@PathVariable Long perfilId, Principal pr) throws Exception {
        Perfil p = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        
        // Limpieza defensiva de corchetes, paréntesis o diagonales dobles
        String urlLimpia = baseUrl.replaceAll("\\[|\\]|\\(|\\)", "").trim().replaceAll("/+$", "");
        String urlFinal = urlLimpia + "/emergencia/" + p.getCodigoEmergencia();
        
        return qrImage.png(urlFinal, 300);
    }

    @GetMapping(value = "/consulta/{codigo}.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] pngConsulta(@PathVariable Long perfilId, @PathVariable String codigo,
                              Principal pr) throws Exception {
        perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        
        // Limpieza defensiva de corchetes, paréntesis o diagonales dobles
        String urlLimpia = baseUrl.replaceAll("\\[|\\]|\\(|\\)", "").trim().replaceAll("/+$", "");
        String urlFinal = urlLimpia + "/consulta/" + codigo;
        
        return qrImage.png(urlFinal, 300);
    }
}
