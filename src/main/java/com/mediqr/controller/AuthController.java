package com.mediqr.controller;

import com.mediqr.dto.RegistroForm;
import com.mediqr.repository.UsuarioRepository;
import com.mediqr.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarios;

    public AuthController(UsuarioService usuarioService, UsuarioRepository usuarios) {
        this.usuarioService = usuarioService;
        this.usuarios = usuarios;
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("form", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroForm form, BindingResult errores) {
        if (!errores.hasFieldErrors("correo")
                && usuarios.existsByCorreo(form.getCorreo().trim().toLowerCase())) {
            errores.rejectValue("correo", "duplicado", "Ese correo ya está registrado");
        }
        if (errores.hasErrors()) return "registro";
        usuarioService.registrar(form);
        return "redirect:/login?registrado";
    }
}
