package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.Order;
import com.example.tecnoWebEmail.Models.OrderDetail;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderCommand {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailResponseService emailResponseService;

    private static final String MINI_SEPARATOR = "------------------------";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Maneja el comando LISORD []
     */
    public String handleListOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            return formatListOrderResponse(orders, "LISORD");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar órdenes: " + e.getMessage(), "LISORD");
        }
    }

    /**
     * Maneja el comando BUSORD ["id"]
     */
    public String handleSearchOrder(String[] parameters) {
        String command = "BUSORD";
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [id] faltante.", command);
            }
            Long orderId = Long.parseLong(parameters[0].trim());

            // ¡CAMBIO CLAVE!
            Order order = orderService.getOrderByIdWithDetails(orderId)
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + orderId));

            // Ahora 'formatSingleOrderResponse' no fallará
            return formatSingleOrderResponse(order, "ORDEN ENCONTRADA", command);
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al buscar orden: " + e.getMessage(), command);
        }
    }

    /**
     * Maneja el comando INSORD (¡Modificado!)
     * Formato: ["ci_cliente", "ci_usuario", "CondicionPago"]
     * (Ej: INSORD["123456", "789012", "Credit"])
     */
    public String handleInsertOrder(String[] parameters) {
        String command = "INSORD";
        try {
            if (parameters.length < 3) {
                return emailResponseService.formatErrorResponse(
                        "Número incorrecto de parámetros. Se esperaban 3: [ciCliente, ciUsuario, condPago]",
                        command);
            }

            // 1. Extraer parámetros
            String clientCi = parameters[0].trim();
            String userCi = parameters[1].trim();
            String paymentCondition = parameters[2].trim(); // "Cash" o "Credit"

            // 2. Llamar al servicio simplificado
            Order createdOrder = orderService.createOrderHeader(
                    clientCi,
                    userCi,
                    paymentCondition
            );

            return formatSingleOrderResponse(createdOrder, "BORRADOR DE ORDEN CREADO EXITOSAMENTE", command);

        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al crear la orden: " + e.getMessage(), command);
        }
    }

    /**
     * Maneja el comando CONFORD (¡Nuevo!)
     * Formato: ["order_id", "num_cuotas"]
     * (Ej: CONFORD["12", "3"] o CONFORD["13", "0"] si es al contado)
     */
    public String handleConfirmOrder(String[] parameters) {
        String command = "CONFORD";
        try {
            // ... (el código se mantiene igual que en la respuesta anterior) ...

            Long orderId = Long.parseLong(parameters[0].trim());
            Integer numInstallments = Integer.parseInt(parameters[1].trim());

            // 1. Confirmamos la orden.
            orderService.confirmOrderAndCreateInstallments(orderId, numInstallments);

            // 2. Volvemos a pedir la orden con todos los datos
            Order confirmedOrderWithDetails = orderService.getOrderByIdWithDetails(orderId)
                    .orElseThrow(() -> new RuntimeException("Error fatal: No se pudo recargar la orden " + orderId));

            // 3. Pasamos la orden "completa" al formateador.
            return formatSingleOrderResponse(confirmedOrderWithDetails, "ORDEN CONFIRMADA Y LISTA PARA PRODUCCIÓN", command);

        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al confirmar la orden: " + e.getMessage(), command);
        }
    }


    // --- Helpers de Formato de Respuesta ---

    private String formatSingleOrderResponse(Order order, String message, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));
        response.append(" ").append(message).append("\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("   • ID de Orden: ").append(order.getId()).append("\n");
        response.append("   • Cliente: ").append(order.getClient().getFirstName()).append(" (CI: ").append(order.getClient().getCi()).append(")\n");
        response.append("   • Vendedor: ").append(order.getUser().getUsername()).append(" (CI: ").append(order.getUser().getCi()).append(")\n");
        response.append("   • Fecha: ").append(order.getOrderDate().format(DATE_FORMATTER)).append("\n");
        response.append("   • Condición: ").append(order.getPaymentCondition()).append("\n");
        response.append("   • Estado Pago: ").append(order.getPaymentState()).append("\n");
        response.append("   • Estado: ").append(order.getStatus()).append("\n"); // "Draft" o "Pending"

        // Los detalles pueden no estar cargados o no existir si es un borrador
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            response.append("\n   DETALLES DEL PEDIDO:\n");
            for (OrderDetail detail : order.getOrderDetails()) {
                response.append("   - (ID Detalle: ").append(detail.getId()).append(") ");
                response.append(detail.getProduct().getNombre()) // Asumiendo getNombre()
                        .append(" | Cant: ").append(detail.getQuantity())
                        .append(" | P. Unit.: ").append(detail.getUnitPrice()).append("\n");
            }
        } else {
            response.append("\n   (Sin detalles adjuntos aún)\n");
        }

        response.append("\n   • MONTO TOTAL: ").append(order.getTotalAmount()).append("\n");
        response.append("\nOK\n");
        return response.toString();
    }

    private String formatListOrderResponse(List<Order> orders, String command) {
        // (Este método se mantiene igual que en tu versión original)
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));
        if (orders.isEmpty()) {
            response.append("RESULTADO DEL LISTADO\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append(" No se encontraron órdenes en la base de datos.\n");
        } else {
            response.append(" LISTADO DE ÓRDENES\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append("Total de registros encontrados: ").append(orders.size()).append("\n\n");
            int contador = 1;
            for (Order order : orders) {
                response.append(" ORDEN #").append(contador).append(" (ID: ").append(order.getId()).append(")\n");
                response.append("   • Cliente: ").append(order.getClient().getFirstName()).append("\n");
                response.append("   • Fecha: ").append(order.getOrderDate().format(DATE_FORMATTER)).append("\n");
                response.append("   • Total: ").append(order.getTotalAmount()).append("\n");
                response.append("   • Estado Pago: ").append(order.getPaymentState()).append("\n");
                response.append("   • Estado Prod.: ").append(order.getStatus()).append("\n\n");
                contador++;
            }
        }
        response.append("OK\n");
        return response.toString();
    }
}