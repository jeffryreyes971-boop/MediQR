package com.mediqr.controller;

import com.mediqr.model.Perfil;
import com.mediqr.model.Usuario;
import com.mediqr.repository.*;
import com.mediqr.service.DocumentoService;
import com.mediqr.service.PerfilService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PerfilController {

    private final PerfilService perfilService;
    private final PerfilRepository perfiles;
    private final UsuarioRepository usuarios;
    private final AlergiaRepository alergias;
    private final RecetaRepository recetas;
    private final VacunaRepository vacunas;
    private final EnfermedadRepository enfermedades;
    private final DocumentoRepository documentos;
    private final DocumentoService documentoService;

    public PerfilController(PerfilService perfilService, PerfilRepository perfiles,
                            UsuarioRepository usuarios, AlergiaRepository alergias,
                            RecetaRepository recetas, VacunaRepository vacunas,
                            EnfermedadRepository enfermedades,
                            DocumentoRepository documentos, DocumentoService documentoService) {
        this.documentos = documentos;
        this.documentoService = documentoService;
        this.perfilService = perfilService;
        this.perfiles = perfiles;
        this.usuarios = usuarios;
        this.alergias = alergias;
        this.recetas = recetas;
        this.vacunas = vacunas;
        this.enfermedades = enfermedades;
    }

    /** Pantalla de inicio: todos los perfiles de la cuenta. */
    @GetMapping("/dashboard")
    public String dashboard(Principal pr, Model model) {
        model.addAttribute("perfiles", perfiles.findByUsuarioCorreo(pr.getName()));
        return "dashboard";
    }

    @GetMapping("/perfiles/nuevo")
    public String nuevo(Model model) {
        Perfil p = new Perfil();
        p.setParentesco(Perfil.Parentesco.HIJO);
        model.addAttribute("perfil", p);
        return "perfiles/nuevo";
    }

    @PostMapping("/perfiles")
    public String crear(@ModelAttribute("perfil") Perfil form, Principal pr, RedirectAttributes flash) {
        if (form.getNombre() == null || form.getNombre().isBlank()) {
            flash.addFlashAttribute("error", "El nombre del perfil es obligatorio.");
            return "redirect:/perfiles/nuevo";
        }
        Usuario u = usuarios.findByCorreo(pr.getName()).orElseThrow();
        Perfil p = new Perfil();                       // se crea uno nuevo: no se confía en el formulario
        p.setUsuario(u);
        p.setNombre(form.getNombre().trim());
        p.setParentesco(form.getParentesco());
        p.setFechaNacimiento(form.getFechaNacimiento());
        String sangre = form.getTipoSangre();
        p.setTipoSangre(sangre == null || sangre.isBlank() ? null : sangre);
        perfiles.save(p);
        return "redirect:/perfiles/" + p.getId();
    }

    /** Formulario para editar los datos básicos de un perfil. */
    @GetMapping("/perfiles/{perfilId}/editar")
    public String editar(@PathVariable Long perfilId, Principal pr, Model model) {
        model.addAttribute("perfil", perfilService.obtenerSiEsDueno(perfilId, pr.getName()));
        return "perfiles/editar";
    }

    @PostMapping("/perfiles/{perfilId}/editar")
    public String actualizar(@PathVariable Long perfilId, @ModelAttribute("form") Perfil form,
                             Principal pr, RedirectAttributes flash) {
        Perfil p = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        if (form.getNombre() == null || form.getNombre().isBlank()) {
            flash.addFlashAttribute("error", "El nombre no puede quedar vacío.");
            return "redirect:/perfiles/" + perfilId + "/editar";
        }
        p.setNombre(form.getNombre().trim());
        p.setFechaNacimiento(form.getFechaNacimiento());
        String sangre = form.getTipoSangre();
        p.setTipoSangre(sangre == null || sangre.isBlank() ? null : sangre);
        // El perfil del titular siempre sigue siendo TITULAR; los demás pueden cambiar de parentesco
        if (p.getParentesco() != Perfil.Parentesco.TITULAR
                && form.getParentesco() != null
                && form.getParentesco() != Perfil.Parentesco.TITULAR) {
            p.setParentesco(form.getParentesco());
        }
        perfiles.save(p);
        flash.addFlashAttribute("ok", "Perfil actualizado.");
        return "redirect:/perfiles/" + perfilId;
    }

    /** Elimina un perfil familiar y todo su historial. El perfil del titular no se puede borrar. */
    @PostMapping("/perfiles/{perfilId}/eliminar")
    public String eliminar(@PathVariable Long perfilId, Principal pr, RedirectAttributes flash) {
        Perfil p = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        if (p.getParentesco() == Perfil.Parentesco.TITULAR) {
            flash.addFlashAttribute("error", "El perfil principal de tu cuenta no se puede eliminar.");
            return "redirect:/dashboard";
        }
        documentos.findByPerfilIdOrderByCreadoEnDesc(perfilId).forEach(documentoService::borrarArchivo);
        perfiles.delete(p);   // la base de datos borra en cascada alergias, recetas, vacunas, etc.
        flash.addFlashAttribute("ok", "Perfil eliminado.");
        return "redirect:/dashboard";
    }

    /** Resumen del perfil con contadores. */
    @GetMapping("/perfiles/{perfilId}")
    public String inicio(@PathVariable Long perfilId, Principal pr, Model model) {
        Perfil perfil = perfilService.obtenerSiEsDueno(perfilId, pr.getName());
        model.addAttribute("perfil", perfil);
        model.addAttribute("alergias", alergias.findByPerfilId(perfilId));
        model.addAttribute("recetas", recetas.findByPerfilIdAndActivaTrue(perfilId));
        model.addAttribute("vacunas", vacunas.findByPerfilIdOrderByFechaAplicacionDesc(perfilId));
        model.addAttribute("enfermedades", enfermedades.findByPerfilId(perfilId));
        return "perfiles/inicio";
    }
}
