package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Supply;
import com.example.tecnoWebEmail.Models.SupplyMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SupplyMovementRepository extends JpaRepository<SupplyMovement, Long> {
    
    // Buscar movimientos por insumo
    List<SupplyMovement> findBySupply(Supply supply);
    
    // Buscar por tipo de movimiento
    List<SupplyMovement> findByMovementType(SupplyMovement.MovementType movementType);
    
    // Buscar por rango de fechas
    List<SupplyMovement> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Buscar por insumo y tipo de movimiento
    List<SupplyMovement> findBySupplyAndMovementType(Supply supply, SupplyMovement.MovementType movementType);
    
    // Buscar por referencia (ej: orden de producción o compra)
    List<SupplyMovement> findByReferenceId(Long referenceId);
    
    // Buscar movimientos recientes primero
    List<SupplyMovement> findBySupplyOrderByDateDesc(Supply supply);
}