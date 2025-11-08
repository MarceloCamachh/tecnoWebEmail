package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.Installment;
import com.example.tecnoWebEmail.Models.Order;
import com.example.tecnoWebEmail.Models.Payment;
import com.example.tecnoWebEmail.Repository.OrderRepository;
import com.example.tecnoWebEmail.Repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InstallmentService installmentService;

    /**
     * Crea un nuevo pago y actualiza los estados del Pedido y la Cuota.
     * Este método es transaccional. Si falla la actualización del pedido,
     * el pago no se guardará.
     *
     * @param paymentData El objeto de pago (parcial) con el monto,
     * el ID del pedido (payment.getOrder().getId()) y
     * opcionalmente el ID de la cuota (payment.getInstallment().getId())
     * @return El pago guardado.
     */
    @Transactional(rollbackFor = Exception.class)
    public Payment createPayment(Payment paymentData) {

        // 1. Validar y obtener el Pedido (Order)
        Long orderId = paymentData.getOrder().getId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // 2. Preparar el objeto Payment
        paymentData.setOrder(order);
        paymentData.setPaymentDate(LocalDateTime.now());

        // 3. Validar y asignar la Cuota (Installment), si aplica
        if (paymentData.getInstallment() != null && paymentData.getInstallment().getId() != null) {
            Long installmentId = paymentData.getInstallment().getId();

            // Usamos el repositorio de Installment para buscarla
            Installment installment = installmentService.getInstallmentById(installmentId)
                    .orElseThrow(() -> new RuntimeException("Installment not found with id: " + installmentId));

            // Validación de seguridad: Asegurar que la cuota pertenezca al pedido
            if (!installment.getOrder().getId().equals(orderId)) {
                throw new IllegalStateException("Installment does not belong to the specified order.");
            }
            paymentData.setInstallment(installment);
        }

        // 4. Guardar el nuevo pago
        Payment savedPayment = paymentRepository.save(paymentData);

        // 5. --- LÓGICA CLAVE: Actualizar el Pedido (Order) ---
        // Recalculamos el total pagado para el pedido
        BigDecimal newTotalPaidForOrder = paymentRepository.getTotalPaidForOrder(orderId);
        order.setAmountPaid(newTotalPaidForOrder);

        // Actualizamos el estado de PAGO del pedido
        if (newTotalPaidForOrder.compareTo(order.getTotalAmount()) >= 0) {
            order.setPaymentState("Paid");
        } else if (newTotalPaidForOrder.compareTo(BigDecimal.ZERO) > 0) {
            order.setPaymentState("Partial");
        } else {
            order.setPaymentState("Pending");
        }
        orderRepository.save(order); // Guardar el estado actualizado del pedido

        // 6. --- LÓGICA CLAVE: Actualizar la Cuota (Installment) ---
        if (savedPayment.getInstallment() != null) {
            // Llamamos al servicio de cuotas para que actualice su propio estado
            installmentService.addPaymentToInstallment(
                    savedPayment.getInstallment().getId(),
                    savedPayment.getAmount()
            );
        }

        return savedPayment;
    }

    // --- Métodos de Lectura ---

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> findByIdWithDetails(Long id) {
        return paymentRepository.findByIdWithDetails(id);
    }


    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> findAllWithDetails() {
        return paymentRepository.findAllWithDetails();
    }

    /**
     * Obtiene todos los pagos asociados a un ID de pedido.
     */
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderIdWithDetails(orderId);
    }

    /**
     * Obtiene todos los pagos (abonos) asociados a un ID de cuota.
     */
    public List<Payment> getPaymentsByInstallmentId(Long installmentId) {
        Installment installment = installmentService.getInstallmentById(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found with id: " + installmentId));
        return paymentRepository.findByInstallment(installment);
    }

    public List<Payment> findByInstallmentIdWithDetails(Long installmentId) {
        return paymentRepository.findByInstallmentIdWithDetails(installmentId);
    }

    /**
     * Obtiene el monto total pagado para un pedido.
     */
    public BigDecimal getTotalPaidForOrder(Long orderId) {
        // Validamos que el pedido exista
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        return paymentRepository.getTotalPaidForOrder(orderId);
    }
}