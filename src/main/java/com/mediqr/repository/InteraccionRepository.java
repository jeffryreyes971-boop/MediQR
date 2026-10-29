package com.mediqr.repository;

import com.mediqr.model.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InteraccionRepository extends JpaRepository<InteraccionMedicamento, Long> {

    @Query("SELECT i FROM InteraccionMedicamento i WHERE "
         + "(LOWER(i.medicamentoA) = LOWER(:a) AND LOWER(i.medicamentoB) = LOWER(:b)) OR "
         + "(LOWER(i.medicamentoA) = LOWER(:b) AND LOWER(i.medicamentoB) = LOWER(:a))")
    List<InteraccionMedicamento> buscarPar(@Param("a") String a, @Param("b") String b);
}
