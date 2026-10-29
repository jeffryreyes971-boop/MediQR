package com.mediqr.repository;

import com.mediqr.model.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecetaRepository extends JpaRepository<Receta, Long> {

    List<Receta> findByPerfilIdOrderByFechaRecetaDesc(Long perfilId);
    List<Receta> findByPerfilIdAndActivaTrue(Long perfilId);
    List<Receta> findByPerfilIdAndActivaTrueAndCriticoTrue(Long perfilId);
}
