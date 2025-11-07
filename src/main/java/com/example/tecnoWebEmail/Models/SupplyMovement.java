package com.example.tecnoWebEmail.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "supply_movements")
public class SupplyMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supply_movement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id", nullable = false)
    private Supply supply;

    @Column(name = "movement_type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private MovementType movementType;

    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(name = "date", nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reason", length = 255)
    private String reason;

    // Enum para el tipo de movimiento
    public enum MovementType {
        ENTRY,      // Entrada
        EXIT,       // Salida
        ADJUSTMENT  // Ajuste
    }

    // Constructor vacío
    public SupplyMovement() {}

    // Constructor con parámetros
    public SupplyMovement(Supply supply, MovementType movementType, BigDecimal quantity, 
                         Long referenceId, String reason) {
        this.supply = supply;
        this.movementType = movementType;
        this.quantity = quantity;
        this.referenceId = referenceId;
        this.reason = reason;
        this.date = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Supply getSupply() {
        return supply;
    }

    public void setSupply(Supply supply) {
        this.supply = supply;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "SupplyMovement{" +
                "id=" + id +
                ", supply=" + (supply != null ? supply.getId() : null) +
                ", movementType=" + movementType +
                ", quantity=" + quantity +
                ", date=" + date +
                ", referenceId=" + referenceId +
                ", reason='" + reason + '\'' +
                '}';
    }
}
