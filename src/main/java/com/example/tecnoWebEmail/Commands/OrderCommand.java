package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.Order;
import com.example.tecnoWebEmail.Models.OrderDetail;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderCommand {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailResponseService emailResponseService;

    private static final String MINI_SEPARATOR = "------------------------";

    /**
     * Maneja el comando LISORD
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
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [id] faltante.", "BUSORD");
            }
            Long orderId = Long.parseLong(parameters[0].trim());

            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + orderId));

            return formatSingleOrderResponse(order, "ORDEN ENCONTRADA", "BUSORD");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al buscar orden: " + e.getMessage(), "BUSORD");
        }
    }

    /**
     * Maneja el comando INSORD
     * Formato: ["ci_cliente", "ci_usuario", "CondicionPago", "NumCuotas", "idProd1:cant1,idProd2:cant2,..."]
     */
    public String handleInsertOrder(String[] parameters) {
        try {
            if (parameters.length < 5) {
                return emailResponseService.formatErrorResponse(
                        "Número incorrecto de parámetros. Se esperaban 5: [ciCliente, ciUsuario, condPago, numCuotas, \"detalles\"]",
                        "INSORD");
            }

            // 1. Extraer parámetros
            String clientCi = parameters[0].trim();
            String userCi = parameters[1].trim();
            String paymentCondition = parameters[2].trim(); // "Cash" o "Credit"
            Integer numInstallments = Integer.parseInt(parameters[3].trim());
            Map<Long, Integer> productDetails = getProductDetails(parameters);

            // 3. Llamar al servicio modificado
            Order createdOrder = orderService.createOrder(
                    paymentCondition,
                    productDetails,
                    clientCi,
                    userCi,
                    numInstallments
            );

            return formatSingleOrderResponse(createdOrder, "ORDEN CREADA EXITOSAMENTE", "INSORD");

        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al crear la orden: " + e.getMessage(), "INSORD");
        }
    }

    private static Map<Long, Integer> getProductDetails(String[] parameters) {
        String detailsString = parameters[4].trim();

        // 2. Parsear el string de detalles (ej: "1:5,3:10")
        Map<Long, Integer> productDetails = new HashMap<>();
        if (detailsString.isEmpty()) {
            throw new RuntimeException("La lista de detalles de productos no puede estar vacía.");
        }

        String[] pairs = detailsString.split(",");
        for (String pair : pairs) {
            String[] parts = pair.split(":");
            if (parts.length != 2) {
                throw new RuntimeException("Formato de detalle incorrecto. Use 'idProducto:cantidad'.");
            }
            Long productId = Long.parseLong(parts[0]);
            Integer quantity = Integer.parseInt(parts[1]);
            productDetails.put(productId, quantity);
        }
        return productDetails;
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
        response.append("   • Fecha: ").append(order.getOrderDate().toLocalDate()).append("\n");
        response.append("   • Condición: ").append(order.getPaymentCondition()).append("\n");
        response.append("   • Estado Pago: ").append(order.getPaymentState()).append("\n");
        response.append("   • Estado Prod.: ").append(order.getStatus()).append("\n");

        response.append("\n   DETALLES DEL PEDIDO:\n");
        for (OrderDetail detail : order.getOrderDetails()) {
            response.append("   - Producto: ").append(detail.getProduct().getNombre()) // Asumiendo getNombre()
                    .append(" (ID: ").append(detail.getProduct().getId()).append(")\n");
            response.append("     Cantidad: ").append(detail.getQuantity()).append("\n");
            response.append("     P. Unit.: ").append(detail.getUnitPrice()).append("\n");
        }

        response.append("\n   • MONTO TOTAL: ").append(order.getTotalAmount()).append("\n");
        response.append("\nOK\n");
        return response.toString();
    }

    private String formatListOrderResponse(List<Order> orders, String command) {
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
                response.append("   • Fecha: ").append(order.getOrderDate().toLocalDate()).append("\n");
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