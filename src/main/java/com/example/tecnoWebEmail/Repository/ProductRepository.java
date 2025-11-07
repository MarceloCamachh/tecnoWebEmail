package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// Buscar producto por su SKU único
	Product findBySku(String sku);

}
