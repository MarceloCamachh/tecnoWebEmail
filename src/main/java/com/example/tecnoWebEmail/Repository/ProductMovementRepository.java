package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Product;
import com.example.tecnoWebEmail.Models.ProductMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductMovementRepository extends JpaRepository<ProductMovement, Long> {
    
    // Buscar movimientos por producto
    List<ProductMovement> findByProduct(Product product);
    
    // Buscar por tipo de movimiento
    List<ProductMovement> findByMovementType(ProductMovement.MovementType movementType);
    
    // Buscar por rango de fechas
    List<ProductMovement> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Buscar por producto y tipo de movimiento
    List<ProductMovement> findByProductAndMovementType(Product product, ProductMovement.MovementType movementType);
    
    // Buscar por referencia (ej: orden de producción o pedido)
    List<ProductMovement> findByReferenceId(Long referenceId);
    
    // Buscar movimientos recientes primero
    List<ProductMovement> findByProductOrderByDateDesc(Product product);
}