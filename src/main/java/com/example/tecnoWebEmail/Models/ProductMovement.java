package com.example.tecnoWebEmail.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_movements")
public class ProductMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_movement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "movement_type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

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
    public ProductMovement() {}

    // Constructor con parámetros
    public ProductMovement(Product product, MovementType movementType, Integer quantity, 
                         Long referenceId, String reason) {
        this.product = product;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
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
        return "ProductMovement{" +
                "id=" + id +
                ", product=" + (product != null ? product.getId() : null) +
                ", movementType=" + movementType +
                ", quantity=" + quantity +
                ", date=" + date +
                ", referenceId=" + referenceId +
                ", reason='" + reason + '\'' +
                '}';
    }
}
