package com.mediqr.service;

import com.mediqr.dto.RegistroForm;
import com.mediqr.model.Perfil;
import com.mediqr.model.Usuario;
import com.mediqr.repository.PerfilRepository;
import com.mediqr.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PerfilRepository perfiles;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PerfilRepository perfiles,
                          PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.encoder = encoder;
    }

    @Transactional
    public void registrar(RegistroForm form) {
        Usuario u = new Usuario();
        u.setNombre(form.getNombre().trim());
        u.setCorreo(form.getCorreo().trim().toLowerCase());
        u.setPasswordHash(encoder.encode(form.getPassword()));   // BCrypt
        usuarios.save(u);

        // Todo usuario nace con su perfil de titular
        Perfil p = new Perfil();
        p.setUsuario(u);
        p.setNombre(u.getNombre());
        perfiles.save(p);
    }
}
