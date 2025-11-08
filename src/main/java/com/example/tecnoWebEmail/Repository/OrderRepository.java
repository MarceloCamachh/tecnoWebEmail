package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Client;
import com.example.tecnoWebEmail.Models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.client c " +
            "JOIN FETCH o.user u " +
            "LEFT JOIN FETCH o.orderDetails d " +
            "LEFT JOIN FETCH d.product p " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithAllDetails(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Order o JOIN FETCH o.client c")
    List<Order> findAllWithClient();
}