package com.mediqr.controller;

import com.mediqr.model.Documento;
import com.mediqr.model.Perfil;
import com.mediqr.repository.DocumentoRepository;
import com.mediqr.service.DocumentoService;
import com.mediqr.service.PerfilService;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfiles/{perfilId}/documentos")
public class DocumentoController {

    private final PerfilService perfilService;
    private final DocumentoRepository documentos;
    private final DocumentoService documentoService;

    public DocumentoController(PerfilService perfilService, DocumentoRepository documentos,
                               DocumentoService documentoService) {
        this.perfilService = perfilService;
        this.documentos = documentos;
        this.documentoService = documentoService;
    }

    @GetMapping
    public String listar(@PathVariable Long perfilId, Principal pr, Model model) {
        model.addAttribute("perfil", perfilService.obtenerSiEsDueno(perfilId, pr.getName()));
        model.addAttribute("documentos", documentos.findByPerfilIdOrderByCreadoEnDesc(perfilId));
        return "documentos/lista";
    }

    @PostMapping
    public String subir(@PathVariable Long perfilId, @RequestParam("archivo") MultipartFile archivo,
                        Principal pr, RedirectAttributes flash) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        try {
            documentoService.guardar(perfil, archivo);
            flash.addFlashAttribute("ok", "Documento guardado y cifrado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo guardar el documento.");
        }
        return "redirect:/perfiles/" + perfilId + "/documentos";
    }

    @PostMapping("/{docId}/eliminar")
    public String eliminar(@PathVariable Long perfilId, @PathVariable Long docId,
                           Principal pr, RedirectAttributes flash) {
        perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        documentos.findById(docId)
            .filter(x -> x.getPerfil().getId().equals(perfilId))   // evita borrar datos ajenos
            .ifPresent(documentoService::eliminar);
        flash.addFlashAttribute("ok", "Documento eliminado.");
        return "redirect:/perfiles/" + perfilId + "/documentos";
    }

    /** Descifra el archivo y lo muestra en el navegador. */
    @GetMapping("/{docId}")
    public ResponseEntity<byte[]> ver(@PathVariable Long perfilId, @PathVariable Long docId,
                                      Principal pr) throws Exception {
        perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        Documento d = documentos.findById(docId)
            .filter(x -> x.getPerfil().getId().equals(perfilId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(d.getTipoMime()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.inline().filename(d.getNombreOriginal(), StandardCharsets.UTF_8).build().toString())
            .body(documentoService.leer(d));
    }
}
