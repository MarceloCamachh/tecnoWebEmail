package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Installment;
import com.example.tecnoWebEmail.Models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    // Encontrar todas las cuotas de un pedido
    List<Installment> findByOrder(Order order);

    // Encontrar cuotas por estado (ej: "Pending")
    List<Installment> findByState(String state);

    // Encontrar cuotas vencidas y pendientes
    List<Installment> findByDueDateBeforeAndState(LocalDate date, String state);

    // Encontrar cuotas vencidas y pagadas
    List<Installment> findByDueDateBeforeAndStateNot(LocalDate date, String state);
}