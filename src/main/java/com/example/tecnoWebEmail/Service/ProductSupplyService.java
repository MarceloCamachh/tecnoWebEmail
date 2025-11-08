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
        if (requiredAmount == null || requiredAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La cantidad requerida debe ser mayor que cero");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productId));
        Supply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + supplyId));

        ProductSupply productSupply = new ProductSupply(product, supply, requiredAmount);
        return productSupplyRepository.save(productSupply);
    }

    @Transactional
    public void removeSupplyFromProduct(Long productId, Long supplyId) {
        int rows = productSupplyRepository.deleteByProductIdAndSupplyId(productId, supplyId);
        if (rows == 0) {
            throw new RuntimeException("Relación producto-insumo no encontrada");
        }
    }

    @Transactional
    public ProductSupply updateRequiredAmount(Long productId, Long supplyId, BigDecimal newAmount) {
        if (newAmount == null || newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La cantidad requerida debe ser mayor que cero");
        }
        ProductSupply ps = productSupplyRepository
                .findByProductIdAndSupplyIdFetch(productId, supplyId)
                .orElseThrow(() -> new RuntimeException("Relación producto-insumo no encontrada"));
        ps.setRequiredAmount(newAmount);
        return productSupplyRepository.save(ps);
    }

    @Transactional(readOnly = true)
    public List<ProductSupply> getSuppliesForProduct(Long productId) {
        return productSupplyRepository.findAllByProductIdFetchAll(productId);
    }

    @Transactional(readOnly = true)
    public List<ProductSupply> getProductsUsingSupply(Long supplyId) {
        return productSupplyRepository.findAllBySupplyIdFetchAll(supplyId);
    }

    @Transactional(readOnly = true)
    public Map<Supply, BigDecimal> calculateRequiredSupplies(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor que cero");
        }
        List<ProductSupply> supplies = productSupplyRepository.findAllByProductIdFetchAll(productId);

        Map<Supply, BigDecimal> requiredSupplies = new HashMap<>();
        for (ProductSupply ps : supplies) {
            BigDecimal totalRequired = ps.getRequiredAmount().multiply(BigDecimal.valueOf(quantity));
            requiredSupplies.put(ps.getSupply(), totalRequired);
        }
        return requiredSupplies;
    }

    @Transactional(readOnly = true)
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
