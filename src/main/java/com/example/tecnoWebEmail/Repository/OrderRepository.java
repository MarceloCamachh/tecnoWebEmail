package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Client;
import com.example.tecnoWebEmail.Models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Buscar pedidos de un cliente específico
    List<Order> findByClient(Client client);

    // Buscar pedidos por estado de producción
    List<Order> findByStatus(String status);

    // Buscar pedidos por estado de pago (ej: "Pending", "Partial")
    List<Order> findByPaymentState(String paymentState);

    // Buscar pedidos en un rango de fechas
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}