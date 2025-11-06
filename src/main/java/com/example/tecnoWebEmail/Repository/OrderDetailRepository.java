package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Order;
import com.example.tecnoWebEmail.Models.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // Encontrar todas las líneas de un pedido
    List<OrderDetail> findByOrder(Order order);

    // Encontrar todas las veces que un producto ha sido pedido
    List<OrderDetail> findByProduct(Product product);
}