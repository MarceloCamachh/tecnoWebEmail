package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.Installment;
import com.example.tecnoWebEmail.Models.Order;
import com.example.tecnoWebEmail.Repository.InstallmentRepository;
import com.example.tecnoWebEmail.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InstallmentService {

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private OrderRepository orderRepository; // Para buscar el pedido

    /**
     * Lógica principal: Crea las cuotas para un pedido a crédito.
     * Este método debería ser llamado por 'OrderService' cuando se crea un pedido.
     *
     * @param order El pedido recién creado.
     * @param numberOfInstallments El número de cuotas (ej: 3, 6, 12).
     */
    @Transactional
    public void createInstallmentsForOrder(Order order, int numberOfInstallments) {

        if (order == null || !"Credit".equals(order.getPaymentCondition())) {
            throw new IllegalArgumentException("Installments can only be created for 'Credit' orders.");
        }
        if (numberOfInstallments <= 0) {
            throw new IllegalArgumentException("Number of installments must be greater than 0.");
        }

        BigDecimal totalAmount = order.getTotalAmount();
        // Divide el total entre el número de cuotas, redondeando a 2 decimales
        BigDecimal installmentAmount = totalAmount.divide(
                new BigDecimal(numberOfInstallments), 2, RoundingMode.HALF_UP
        );

        // Calcula el 'sobrante' del redondeo
        BigDecimal remainder = totalAmount.subtract(
                installmentAmount.multiply(new BigDecimal(numberOfInstallments))
        );

        List<Installment> installments = new ArrayList<>();
        LocalDate dueDate = LocalDate.now().plusMonths(1); // La primera cuota vence en 1 mes

        for (int i = 1; i <= numberOfInstallments; i++) {
            Installment installment = new Installment();
            installment.setOrder(order);
            installment.setInstallmentNumber(i);
            installment.setDueDate(dueDate.plusMonths(i - 1)); // 1er mes, 2do mes, etc.

            // Si es la última cuota, le suma el sobrante del redondeo
            if (i == numberOfInstallments) {
                installment.setInstallmentAmount(installmentAmount.add(remainder));
            } else {
                installment.setInstallmentAmount(installmentAmount);
            }

            // El estado y 'amountPaid' se setean por defecto en la entidad
            installments.add(installment);
        }

        installmentRepository.saveAll(installments);
    }

    /**
     * Actualiza el estado de una cuota cuando recibe un pago.
     * Este método debería ser llamado por 'PaymentService'.
     *
     * @param installmentId El ID de la cuota a pagar.
     * @param paymentAmount El monto que se está pagando.
     */
    @Transactional
    public void addPaymentToInstallment(Long installmentId, BigDecimal paymentAmount) {
        Installment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found with id: " + installmentId));

        BigDecimal newAmountPaid = installment.getAmountPaid().add(paymentAmount);
        installment.setAmountPaid(newAmountPaid);

        // Compara el monto pagado con el monto de la cuota
        if (newAmountPaid.compareTo(installment.getInstallmentAmount()) >= 0) {
            // Pagado total o de más
            installment.setState("Paid");
        } else {
            // Aún falta pagar
            installment.setState("Partial");
        }

        installmentRepository.save(installment);
    }

    // --- Métodos de Consulta ---

    /**
     * Obtiene una cuota por su ID.
     */
    public Optional<Installment> getInstallmentById(Long id) {
        return installmentRepository.findById(id);
    }

    /**
     * Obtiene todas las cuotas de un pedido específico.
     */
    public List<Installment> getInstallmentsByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return installmentRepository.findByOrder(order);
    }

    /**
     * Obtiene todas las cuotas vencidas que no estén pagadas.
     */
    public List<Installment> getOverdueInstallments() {
        return installmentRepository.findByDueDateBeforeAndStateNot(LocalDate.now(), "Paid");
    }

    /**
     * Obtiene todas las cuotas por estado (ej: "Pending").
     */
    public List<Installment> getInstallmentsByState(String state) {
        return installmentRepository.findByState(state);
    }
}