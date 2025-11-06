package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {

    // Encontrar órdenes de producción por estado
    List<ProductionOrder> findByStatus(String status);

    // Encontrar por fecha de finalización estimada
    List<ProductionOrder> findByEstimatedCompletionDate(String date);
}