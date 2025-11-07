package com.example.tecnoWebEmail.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_supplies_bom")
public class ProductSupply {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id", nullable = false)
    private Supply supply;

    @Column(name = "required_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal requiredAmount;

    // Constructor vacío
    public ProductSupply() {}

    // Constructor con parámetros
    public ProductSupply(Product product, Supply supply, BigDecimal requiredAmount) {
        this.product = product;
        this.supply = supply;
        this.requiredAmount = requiredAmount;
    }

    // Getters y Setters
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Supply getSupply() {
        return supply;
    }

    public void setSupply(Supply supply) {
        this.supply = supply;
    }

    public BigDecimal getRequiredAmount() {
        return requiredAmount;
    }

    public void setRequiredAmount(BigDecimal requiredAmount) {
        this.requiredAmount = requiredAmount;
    }

    @Override
    public String toString() {
        return "ProductSupply{" +
                "product=" + (product != null ? product.getId() : null) +
                ", supply=" + (supply != null ? supply.getId() : null) +
                ", requiredAmount=" + requiredAmount +
                '}';
    }
}
