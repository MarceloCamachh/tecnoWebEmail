package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {

    // Encontrar órdenes de producción por estado
    List<ProductionOrder> findByStatus(String status);

    // Encontrar por fecha de finalización estimada
    List<ProductionOrder> findByEstimatedCompletionDate(LocalDate date);

    @Query("SELECT po FROM ProductionOrder po " +
            "JOIN FETCH po.orderDetail d " +
            "JOIN FETCH d.product p " +
            "WHERE po.id = :id")
    Optional<ProductionOrder> findByIdWithDetails(@Param("id") Long id);
}