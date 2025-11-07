package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.Product;
import com.example.tecnoWebEmail.Models.ProductMovement;
import com.example.tecnoWebEmail.Repository.ProductMovementRepository;
import com.example.tecnoWebEmail.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductMovementRepository productMovementRepository;

    // Operaciones CRUD básicas
    @Transactional
    public Product createProduct(Product product) {
        // Validar si el SKU ya existe
        if (productRepository.findBySku(product.getSku()) != null) {
            throw new RuntimeException("SKU already exists: " + product.getSku());
        }
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Transactional
    public Product updateProduct(Product product) {
        if (!productRepository.existsById(product.getId())) {
            throw new RuntimeException("Product not found with id: " + product.getId());
        }
        return productRepository.save(product);
    }

    // Operaciones de inventario
    @Transactional
    public ProductMovement registerProductEntry(Long productId, int quantity, String reason, Long referenceId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        // Crear movimiento
        ProductMovement movement = new ProductMovement(
                product,
                ProductMovement.MovementType.ENTRY,
                quantity,
                referenceId,
                reason
        );
        
        // Actualizar stock
        product.setStockActual(product.getStockActual() + quantity);
        productRepository.save(product);
        
        return productMovementRepository.save(movement);
    }

    @Transactional
    public ProductMovement registerProductExit(Long productId, int quantity, String reason, Long referenceId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        // Verificar stock suficiente
        if (product.getStockActual() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getSku());
        }
        
        // Crear movimiento
        ProductMovement movement = new ProductMovement(
                product,
                ProductMovement.MovementType.EXIT,
                quantity,
                referenceId,
                reason
        );
        
        // Actualizar stock
        product.setStockActual(product.getStockActual() - quantity);
        productRepository.save(product);
        
        return productMovementRepository.save(movement);
    }

    @Transactional
    public ProductMovement adjustStock(Long productId, int quantity, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        // Crear movimiento de ajuste
        ProductMovement movement = new ProductMovement(
                product,
                ProductMovement.MovementType.ADJUSTMENT,
                quantity,
                null,
                reason
        );
        
        // Actualizar stock
        product.setStockActual(quantity);
        productRepository.save(product);
        
        return productMovementRepository.save(movement);
    }

    // Consultas de movimientos
    public List<ProductMovement> getProductMovements(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return productMovementRepository.findByProductOrderByDateDesc(product);
    }

    public List<ProductMovement> getProductMovementsByType(Long productId, ProductMovement.MovementType type) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return productMovementRepository.findByProductAndMovementType(product, type);
    }
}
