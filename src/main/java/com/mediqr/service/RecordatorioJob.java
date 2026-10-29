package com.mediqr.service;

import com.mediqr.model.Recordatorio;
import com.mediqr.repository.RecordatorioRepository;
import java.time.LocalDateTime;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecordatorioJob {

    private final RecordatorioRepository repo;
    private final JavaMailSender correo;

    public RecordatorioJob(RecordatorioRepository repo, JavaMailSender correo) {
        this.repo = repo;
        this.correo = correo;
    }

    // Cada 15 minutos busca recordatorios vencidos que aún no se han enviado
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void enviarPendientes() {
        for (Recordatorio r : repo.findByEnviadoFalseAndFechaHoraBefore(LocalDateTime.now())) {
            try {
                SimpleMailMessage m = new SimpleMailMessage();
                m.setTo(r.getPerfil().getUsuario().getCorreo());
                m.setSubject("MediQR: " + r.getTitulo());
                m.setText("Recordatorio para " + r.getPerfil().getNombre() + ": " + r.getTitulo());
                correo.send(m);
                r.setEnviado(true);
                repo.save(r);
            } catch (Exception e) {
                System.err.println("No se pudo enviar el recordatorio " + r.getId() + ": " + e.getMessage());
            }
        }
    }
}
