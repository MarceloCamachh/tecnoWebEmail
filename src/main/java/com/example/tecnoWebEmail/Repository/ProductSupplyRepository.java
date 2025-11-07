package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Product;
import com.example.tecnoWebEmail.Models.ProductSupply;
import com.example.tecnoWebEmail.Models.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSupplyRepository extends JpaRepository<ProductSupply, Long> {
    
    // Encontrar todos los insumos de un producto
    List<ProductSupply> findByProduct(Product product);
    
    // Encontrar todos los productos que usan un insumo
    List<ProductSupply> findBySupply(Supply supply);
    
}