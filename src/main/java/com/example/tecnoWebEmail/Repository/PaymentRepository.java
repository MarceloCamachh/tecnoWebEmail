package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Installment;
import com.example.tecnoWebEmail.Models.Order;
import com.example.tecnoWebEmail.Models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Encontrar todos los pagos de un pedido (Order)
    List<Payment> findByOrder(Order order);

    // Encontrar todos los pagos (abonos) de una cuota (Installment) específica
    List<Payment> findByInstallment(Installment installment);

    // Encontrar pagos en un rango de fechas
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Sumar el total pagado para un pedido
    // Corresponde a sumar todos los 'monto' donde el 'pedido_id' sea el mismo.
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order.id = :orderId")
    BigDecimal getTotalPaidForOrder(@Param("orderId") Long orderId);

    // Sumar el total pagado para una cuota
    // Corresponde a sumar todos los 'monto' donde el 'cuota_id' sea el mismo.
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.installment.id = :installmentId")
    BigDecimal getTotalPaidForInstallment(@Param("installmentId") Long installmentId);

    /**
     * Para LISPAG: Trae todos los pagos con sus Pedidos y Cuotas (opcional).
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.order o LEFT JOIN FETCH p.installment i")
    List<Payment> findAllWithDetails();

    /**
     * Para BUSPAG: Trae un pago por ID con sus Pedidos y Cuotas (opcional).
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.order o LEFT JOIN FETCH p.installment i WHERE p.id = :id")
    Optional<Payment> findByIdWithDetails(@Param("id") Long id);

    /**
     * Para LISPEDPAG: Trae pagos por ID de Pedido, con Pedidos y Cuotas.
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.order o LEFT JOIN FETCH p.installment i WHERE o.id = :orderId")
    List<Payment> findByOrderIdWithDetails(@Param("orderId") Long orderId);

    /**
     * Para LISCUPAG: Trae pagos por ID de Cuota, con Pedidos y Cuotas.
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.order o JOIN FETCH p.installment i WHERE i.id = :installmentId")
    List<Payment> findByInstallmentIdWithDetails(@Param("installmentId") Long installmentId);

}