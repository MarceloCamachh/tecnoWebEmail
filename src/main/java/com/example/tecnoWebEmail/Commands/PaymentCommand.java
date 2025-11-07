package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.Installment;
import com.example.tecnoWebEmail.Models.Order;
import com.example.tecnoWebEmail.Models.Payment;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PaymentCommand {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EmailResponseService emailResponseService;

    private static final String MINI_SEPARATOR = "------------------------";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Maneja el comando INSPAG
     * Formato: ["order_id", "amount", "payment_type", "installment_id"]
     * (Usar "0" para installment_id si no aplica)
     */
    public String handleInsertPayment(String[] parameters) {
        String command = "INSPAG";
        try {
            if (parameters.length < 4) {
                return emailResponseService.formatErrorResponse(
                        "Número incorrecto de parámetros. Se esperaban 4: [order_id, amount, payment_type, installment_id(0 si N/A)]",
                        command);
            }

            // 1. Parsear parámetros
            Long orderId = Long.parseLong(parameters[0].trim());
            BigDecimal amount = new BigDecimal(parameters[1].trim());
            String paymentType = parameters[2].trim();
            Long installmentId = Long.parseLong(parameters[3].trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return emailResponseService.formatErrorResponse("El monto (amount) debe ser mayor que cero.", command);
            }

            // 2. Construir el objeto Payment
            Payment newPayment = new Payment();
            newPayment.setAmount(amount);
            newPayment.setPaymentType(paymentType);

            // Asignar el Pedido (solo con ID, el servicio buscará el objeto)
            Order tempOrder = new Order();
            tempOrder.setId(orderId);
            newPayment.setOrder(tempOrder);

            // Asignar la Cuota (opcional)
            if (installmentId > 0) {
                Installment tempInstallment = new Installment();
                tempInstallment.setId(installmentId);
                newPayment.setInstallment(tempInstallment);
            }

            // 3. Llamar al servicio
            // Esta llamada es transaccional y actualizará Order y Installment
            Payment savedPayment = paymentService.createPayment(newPayment);

            return formatSinglePaymentResponse(savedPayment, "PAGO CREADO EXITOSAMENTE", command);

        } catch (NumberFormatException e) {
            return emailResponseService.formatErrorResponse("Error en el formato de [order_id], [amount] o [installment_id]. Deben ser números.", command);
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al crear el pago: " + e.getMessage(), command);
        }
    }

    /**
     * Maneja el comando LISPAG []
     * Lista todos los pagos registrados.
     */
    public String handleListAllPayments() {
        String command = "LISPAG";
        try {
            List<Payment> payments = paymentService.getAllPayments();
            String title = "LISTADO DE TODOS LOS PAGOS";
            return formatListPaymentResponse(payments, title, command);
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar pagos: " + e.getMessage(), command);
        }
    }

    /**
     * Maneja el comando BUSPAG ["id_pago"]
     */
    public String handleSearchPayment(String[] parameters) {
        String command = "BUSPAG";
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [id_pago] faltante.", command);
            }
            Long paymentId = Long.parseLong(parameters[0].trim());

            Payment payment = paymentService.getPaymentById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + paymentId));

            return formatSinglePaymentResponse(payment, "PAGO ENCONTRADO", command);
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al buscar pago: " + e.getMessage(), command);
        }
    }

    /**
     * Maneja el comando LISPEDPAG ["id_pedido"]
     * Lista todos los pagos de un pedido específico.
     */
    public String handleListPaymentsByOrder(String[] parameters) {
        String command = "LISPEDPAG";
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [id_pedido] faltante.", command);
            }
            Long orderId = Long.parseLong(parameters[0].trim());

            List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
            String title = "PAGOS ASOCIADOS AL PEDIDO #" + orderId;
            return formatListPaymentResponse(payments, title, command);
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar pagos por pedido: " + e.getMessage(), command);
        }
    }

    /**
     * Maneja el comando LISCUPAG ["id_cuota"]
     * Lista todos los pagos (abonos) de una cuota específica.
     */
    public String handleListPaymentsByInstallment(String[] parameters) {
        String command = "LISCUPAG";
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [id_cuota] faltante.", command);
            }
            Long installmentId = Long.parseLong(parameters[0].trim());

            List<Payment> payments = paymentService.getPaymentsByInstallmentId(installmentId);
            String title = "PAGOS ASOCIADOS A LA CUOTA #" + installmentId;
            return formatListPaymentResponse(payments, title, command);
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar pagos por cuota: " + e.getMessage(), command);
        }
    }

    // --- Helpers de Formato de Respuesta ---

    private String formatSinglePaymentResponse(Payment payment, String message, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));
        response.append(" ").append(message).append("\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("   • ID Pago: ").append(payment.getId()).append("\n");
        response.append("   • ID Pedido: ").append(payment.getOrder().getId()).append("\n");

        String installmentId = (payment.getInstallment() != null)
                ? payment.getInstallment().getId().toString()
                : "N/A";
        response.append("   • ID Cuota: ").append(installmentId).append("\n");

        response.append("   • Monto: ").append(payment.getAmount()).append("\n");
        response.append("   • Tipo de Pago: ").append(payment.getPaymentType()).append("\n");
        response.append("   • Fecha: ").append(payment.getPaymentDate().format(DATETIME_FORMATTER)).append("\n");
        response.append("\nOK\n");
        return response.toString();
    }

    private String formatListPaymentResponse(List<Payment> payments, String title, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));
        response.append(" ").append(title).append("\n");
        response.append(MINI_SEPARATOR).append("\n");

        if (payments.isEmpty()) {
            response.append(" No se encontraron pagos que coincidan con los criterios.\n");
        } else {
            response.append("Total de registros encontrados: ").append(payments.size()).append("\n\n");

            int contador = 1;
            for (Payment payment : payments) {
                response.append(" PAGO #").append(contador).append(" (ID: ").append(payment.getId()).append(")\n");
                response.append("   • ID Pedido: ").append(payment.getOrder().getId()).append("\n");

                String installmentId = (payment.getInstallment() != null)
                        ? payment.getInstallment().getId().toString()
                        : "N/A";
                response.append("   • ID Cuota: ").append(installmentId).append("\n");

                response.append("   • Monto: ").append(payment.getAmount()).append("\n");
                response.append("   • Fecha: ").append(payment.getPaymentDate().format(DATETIME_FORMATTER)).append("\n\n");
                contador++;
            }
        }
        response.append("OK\n");
        return response.toString();
    }
}