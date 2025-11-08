package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.ProductionOrder;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.ProductionOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ProductionOrderCommand {

    @Autowired
    private ProductionOrderService productionOrderService;

    @Autowired
    private EmailResponseService emailResponseService;

    private static final String MINI_SEPARATOR = "------------------------";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Maneja el comando INSPROD (Insertar Orden de Producción)
     * Formato: ["order_detail_id", "start_date", "estimated_date"]
     * (Ej: INSPROD["5", "2025-11-10", "2025-11-20"])
     */
    public String handleCreateProductionOrder(String[] parameters) {
        String command = "INSPROD";
        try {
            if (parameters.length < 3) {
                return emailResponseService.formatErrorResponse(
                        "Número incorrecto de parámetros. Se esperaban 3: [order_detail_id, start_date (YYYY-MM-DD), estimated_date (YYYY-MM-DD)]",
                        command);
            }

            // 1. Parsear parámetros
            Long orderDetailId = Long.parseLong(parameters[0].trim());
            LocalDate startDate = LocalDate.parse(parameters[1].trim());
            LocalDate estimatedDate = LocalDate.parse(parameters[2].trim());

            // 2. Llamar al servicio (esto cierra la transacción)
            ProductionOrder prodOrder = productionOrderService.createProductionOrder(orderDetailId, startDate, estimatedDate);

            // Volver a buscar la orden, pero esta vez con todos los datos
            // para el formateador de respuesta.
            ProductionOrder loadedProdOrder = productionOrderService.getProductionOrderWithDetails(prodOrder.getId())
                    .orElseThrow(() -> new RuntimeException("Error fatal: No se pudo recargar la orden de producción " + prodOrder.getId()));

            return formatProductionOrderResponse(loadedProdOrder, "ORDEN DE PRODUCCIÓN CREADA", command);

        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al crear la orden de producción: " + e.getMessage(), command);
        }
    }

    // --- Helper de Formato ---

    private String formatProductionOrderResponse(ProductionOrder po, String message, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));
        response.append(" ").append(message).append("\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("   • ID Orden Prod.: ").append(po.getId()).append("\n");
        response.append("   • ID Detalle Pedido: ").append(po.getOrderDetail().getId()).append("\n");
        response.append("   • Producto a Fabricar: ").append(po.getOrderDetail().getProduct().getNombre()).append("\n");
        response.append("   • Cantidad: ").append(po.getOrderDetail().getQuantity()).append("\n");
        response.append("   • Estado: ").append(po.getStatus()).append("\n");
        response.append("   • Fecha Inicio: ").append(po.getStartDate().format(DATE_FORMATTER)).append("\n");
        response.append("   • Fecha Estimada: ").append(po.getEstimatedCompletionDate().format(DATE_FORMATTER)).append("\n");
        response.append("\nOK\n");
        return response.toString();
    }
}