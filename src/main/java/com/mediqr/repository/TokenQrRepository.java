package com.mediqr.repository;

import com.mediqr.model.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenQrRepository extends JpaRepository<TokenQr, Long> {

    Optional<TokenQr> findByCodigo(String codigo);

    // UPDATE atómico: solo funciona si el código no se ha usado y no ha vencido.
    // Así dos personas no pueden usar el mismo QR al mismo tiempo.
    @Modifying
    @Query("UPDATE TokenQr t SET t.usado = true "
         + "WHERE t.codigo = :codigo AND t.usado = false AND t.expiraEn > :ahora")
    int marcarUsado(@Param("codigo") String codigo, @Param("ahora") LocalDateTime ahora);
}
