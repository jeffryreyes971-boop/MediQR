package com.mediqr.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
            // Desactiva CSRF únicamente en las rutas públicas del QR
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/emergencia/**", "/consulta/**")
            )
            .authorizeHttpRequests(a -> a
                // Rutas PÚBLICAS: cualquiera las puede abrir sin iniciar sesión
                .requestMatchers(
                    "/", 
                    "/login", 
                    "/registro", 
                    "/error",
                    "/css/**", 
                    "/js/**", 
                    "/img/**", 
                    "/favicon.ico",
                    "/emergencia/**", 
                    "/consulta/**",
                    "/perfiles/*/qr/**"
                ).permitAll()
                // Todo lo demás requiere inicio de sesión
                .anyRequest().authenticated()
            )
            .formLogin(f -> f
                .loginPage("/login")
                .usernameParameter("correo")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(l -> l.logoutSuccessUrl("/login?logout"));

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
