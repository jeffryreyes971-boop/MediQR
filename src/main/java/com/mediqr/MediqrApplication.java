package com.mediqr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // activa los recordatorios automáticos por correo
public class MediqrApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediqrApplication.class, args);
    }
}
