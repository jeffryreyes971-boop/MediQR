package com.mediqr.repository;

import com.mediqr.model.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditoriaRepository extends JpaRepository<AuditoriaAcceso, Long> {

    List<AuditoriaAcceso> findByPerfilIdOrderByFechaDesc(Long perfilId);
}
