package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.Product;
import com.example.tecnoWebEmail.Models.ProductSupply;
import com.example.tecnoWebEmail.Models.Supply;
import com.example.tecnoWebEmail.Models.SupplyMovement;
import com.example.tecnoWebEmail.Repository.ProductRepository;
import com.example.tecnoWebEmail.Repository.ProductSupplyRepository;
import com.example.tecnoWebEmail.Repository.SupplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ProductSupplyService {
    
    @Autowired
    private ProductSupplyRepository productSupplyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplyRepository supplyRepository;

    @Autowired
    private SupplyService supplyService;

    @Transactional
    public ProductSupply addSupplyToProduct(Long productId, Long supplyId, BigDecimal requiredAmount) {
        // Validar cantidad
        if (requiredAmount == null || requiredAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La cantidad requerida debe ser mayor que cero");
        }

        // Buscar producto e insumo
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productId));
        
        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + supplyId));

        // Crear la relación
        ProductSupply productSupply = new ProductSupply(product, supply, requiredAmount);
        return productSupplyRepository.save(productSupply);
    }

    @Transactional
    public void removeSupplyFromProduct(Long productId, Long supplyId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productId));
        
        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + supplyId));

        List<ProductSupply> relations = productSupplyRepository.findByProduct(product);
        relations.stream()
                .filter(ps -> ps.getSupply().getId().equals(supplyId))
                .findFirst()
                .ifPresent(productSupplyRepository::delete);
    }

    @Transactional
    public ProductSupply updateRequiredAmount(Long productId, Long supplyId, BigDecimal newAmount) {
        if (newAmount == null || newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La cantidad requerida debe ser mayor que cero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productId));
        
        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + supplyId));

        ProductSupply productSupply = productSupplyRepository.findByProduct(product).stream()
                .filter(ps -> ps.getSupply().getId().equals(supplyId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Relación producto-insumo no encontrada"));

        productSupply.setRequiredAmount(newAmount);
        return productSupplyRepository.save(productSupply);
    }

    @Transactional(readOnly = true)
    public List<ProductSupply> getSuppliesForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productId));
        return productSupplyRepository.findByProduct(product);
    }

    @Transactional(readOnly = true)
    public List<ProductSupply> getProductsUsingSupply(Long supplyId) {
        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + supplyId));
        return productSupplyRepository.findBySupply(supply);
    }

    @Transactional(readOnly = true)
    public Map<Supply, BigDecimal> calculateRequiredSupplies(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor que cero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productId));

        List<ProductSupply> supplies = productSupplyRepository.findByProduct(product);
        Map<Supply, BigDecimal> requiredSupplies = new HashMap<>();

        for (ProductSupply ps : supplies) {
            BigDecimal totalRequired = ps.getRequiredAmount().multiply(BigDecimal.valueOf(quantity));
            requiredSupplies.put(ps.getSupply(), totalRequired);
        }

        return requiredSupplies;
    }

    @Transactional
    public boolean validateSuppliesAvailability(Long productId, Integer quantity) {
        Map<Supply, BigDecimal> requiredSupplies = calculateRequiredSupplies(productId, quantity);
        
        for (Map.Entry<Supply, BigDecimal> entry : requiredSupplies.entrySet()) {
            Supply supply = entry.getKey();
            BigDecimal required = entry.getValue();
            
            if (supply.getStockActual().compareTo(required) < 0) {
                return false;
            }
        }
        
        return true;
    }

    @Transactional
    public void consumeSuppliesForProduction(Long productId, Integer quantity, Long productionOrderId) {
        if (!validateSuppliesAvailability(productId, quantity)) {
            throw new RuntimeException("No hay suficientes insumos disponibles para la producción");
        }

        Map<Supply, BigDecimal> requiredSupplies = calculateRequiredSupplies(productId, quantity);
        
        for (Map.Entry<Supply, BigDecimal> entry : requiredSupplies.entrySet()) {
            Supply supply = entry.getKey();
            BigDecimal required = entry.getValue();
            
            // Registrar el consumo de insumos vinculado a la orden de producción
            supplyService.registerMovement(
                supply.getId(),
                SupplyMovement.MovementType.EXIT,
                required,
                productionOrderId,
                "Consumo para producción de " + quantity + " unidades del producto " + productId
            );
        }
    }
}
