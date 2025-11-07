package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.Supply;
import com.example.tecnoWebEmail.Models.SupplyMovement;
import com.example.tecnoWebEmail.Repository.SupplyRepository;
import com.example.tecnoWebEmail.Repository.SupplyMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SupplyService {
    
    @Autowired
    private SupplyRepository supplyRepository;

    @Autowired
    private SupplyMovementRepository supplyMovementRepository;

    public Supply createSupply(Supply supply) {
        // Validar que el nombre no sea nulo
        if (supply.getNombre() == null || supply.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del insumo no puede estar vacío");
        }

        // Validar que no exista otro insumo con el mismo nombre
        Supply existingSupply = supplyRepository.findByNombre(supply.getNombre());
        if (existingSupply != null) {
            throw new RuntimeException("Ya existe un insumo con el nombre: " + supply.getNombre());
        }

        // Si el stock inicial es nulo, inicializarlo en 0
        if (supply.getStockActual() == null) {
            supply.setStockActual(BigDecimal.ZERO);
        }

        return supplyRepository.save(supply);
    }

    public List<Supply> getAllSupplies() {
        return supplyRepository.findAll();
    }

    public Optional<Supply> getSupplyById(Long id) {
        return supplyRepository.findById(id);
    }

    public Supply getSupplyByName(String nombre) {
        return supplyRepository.findByNombre(nombre);
    }

    public List<Supply> getSuppliesByUnitMeasure(String unidadMedida) {
        return supplyRepository.findByUnidadMedida(unidadMedida);
    }

    @Transactional
    public Supply updateSupply(Long id, Supply supplyDetails) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + id));

        supply.setNombre(supplyDetails.getNombre());
        supply.setDescripcion(supplyDetails.getDescripcion());
        supply.setUnidadMedida(supplyDetails.getUnidadMedida());
        // No actualizamos el stock aquí, eso se hace con métodos específicos

        return supplyRepository.save(supply);
    }

    @Transactional
    public void deleteSupply(Long id) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + id));

        // Validar que no tenga stock antes de eliminar
        if (supply.getStockActual().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("No se puede eliminar un insumo que tiene stock");
        }

        supplyRepository.delete(supply);
    }

    @Transactional
    public Supply adjustStock(Long id, BigDecimal quantity, boolean isAddition, String reason) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + id));

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor que cero");
        }

        BigDecimal newStock;
        SupplyMovement.MovementType movementType;

        if (isAddition) {
            newStock = supply.getStockActual().add(quantity);
            movementType = SupplyMovement.MovementType.ENTRY;
        } else {
            if (supply.getStockActual().compareTo(quantity) < 0) {
                throw new RuntimeException("Stock insuficiente");
            }
            newStock = supply.getStockActual().subtract(quantity);
            movementType = SupplyMovement.MovementType.EXIT;
        }

        // Crear y guardar el movimiento
        SupplyMovement movement = new SupplyMovement(
            supply,
            movementType,
            quantity,
            null, // referenceId no necesario para ajustes manuales
            reason
        );
        supplyMovementRepository.save(movement);

        // Actualizar el stock
        supply.setStockActual(newStock);
        return supplyRepository.save(supply);
    }

    @Transactional(readOnly = true)
    public List<SupplyMovement> getMovementsBySupply(Long supplyId) {
        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + supplyId));
        return supplyMovementRepository.findBySupplyOrderByDateDesc(supply);
    }

    @Transactional(readOnly = true)
    public List<SupplyMovement> getMovementsByType(SupplyMovement.MovementType movementType) {
        return supplyMovementRepository.findByMovementType(movementType);
    }

    @Transactional(readOnly = true)
    public List<SupplyMovement> getMovementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return supplyMovementRepository.findByDateBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<SupplyMovement> getMovementsByReference(Long referenceId) {
        return supplyMovementRepository.findByReferenceId(referenceId);
    }

    @Transactional
    public Supply registerMovement(Long supplyId, SupplyMovement.MovementType movementType, 
                                 BigDecimal quantity, Long referenceId, String reason) {
        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + supplyId));

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor que cero");
        }

        BigDecimal newStock;
        
        switch (movementType) {
            case ENTRY:
                newStock = supply.getStockActual().add(quantity);
                break;
            case EXIT:
                if (supply.getStockActual().compareTo(quantity) < 0) {
                    throw new RuntimeException("Stock insuficiente");
                }
                newStock = supply.getStockActual().subtract(quantity);
                break;
            case ADJUSTMENT:
                newStock = quantity; // En ajuste, la cantidad es el nuevo valor del stock
                break;
            default:
                throw new RuntimeException("Tipo de movimiento no válido");
        }

        // Crear y guardar el movimiento
        SupplyMovement movement = new SupplyMovement(
            supply,
            movementType,
            quantity,
            referenceId,
            reason
        );
        supplyMovementRepository.save(movement);

        // Actualizar el stock
        supply.setStockActual(newStock);
        return supplyRepository.save(supply);
    }
}
