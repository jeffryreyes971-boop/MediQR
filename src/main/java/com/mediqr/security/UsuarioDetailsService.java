package com.mediqr.security;

import com.mediqr.model.Usuario;
import com.mediqr.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repo;

    public UsuarioDetailsService(UsuarioRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario u = repo.findByCorreo(correo.toLowerCase())
            .orElseThrow(() -> new UsernameNotFoundException("No existe: " + correo));
        return org.springframework.security.core.userdetails.User
            .withUsername(u.getCorreo())
            .password(u.getPasswordHash())
            .roles(u.getRol().name())
            .build();
    }
}
