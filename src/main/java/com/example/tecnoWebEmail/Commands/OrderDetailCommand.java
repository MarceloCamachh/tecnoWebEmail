package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.Order;
import com.example.tecnoWebEmail.Models.OrderDetail;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.OrderDetailService;
import com.example.tecnoWebEmail.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderDetailCommand {

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private OrderService orderService; // Para obtener el total actualizado

    @Autowired
    private EmailResponseService emailResponseService;

    private static final String MINI_SEPARATOR = "------------------------";

    /**
     * Maneja el comando ADDET (Añadir Detalle)
     * Formato: ["order_id", "product_id", "quantity"]
     */
    public String handleAddDetailToOrder(String[] parameters) {
        String command = "ADDET";
        try {
            if (parameters.length < 3) {
                return emailResponseService.formatErrorResponse(
                        "Número incorrecto de parámetros. Se esperaban 3: [order_id, product_id, quantity]",
                        command);
            }

            // 1. Parsear parámetros
            Long orderId = Long.parseLong(parameters[0].trim());
            Long productId = Long.parseLong(parameters[1].trim());
            Integer quantity = Integer.parseInt(parameters[2].trim());

            // 2. Llamar al servicio
            OrderDetail savedDetail = orderDetailService.addDetailToOrder(orderId, productId, quantity);

            // 3. Obtener el pedido actualizado para mostrar el nuevo total
            Order updatedOrder = orderService.getOrderById(orderId).get(); // .get() es seguro aquí

            return formatDetailResponse(savedDetail, updatedOrder, command);

        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al añadir detalle: " + e.getMessage(), command);
        }
    }

    // --- Helper de Formato ---

    private String formatDetailResponse(OrderDetail detail, Order order, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));
        response.append(" DETALLE AÑADIDO EXITOSAMENTE\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("   • ID Detalle: ").append(detail.getId()).append("\n");
        response.append("   • Producto: ").append(detail.getProduct().getNombre()).append(" (ID: ").append(detail.getProduct().getId()).append(")\n");
        response.append("   • Cantidad: ").append(detail.getQuantity()).append("\n");
        response.append("   • P. Unit.: ").append(detail.getUnitPrice()).append("\n");
        response.append("\n");
        response.append("   • ID de Orden: ").append(order.getId()).append("\n");
        response.append("   • NUEVO TOTAL (Temporal): ").append(order.getTotalAmount()).append("\n");
        response.append("\nOK\n");
        return response.toString();
    }
}