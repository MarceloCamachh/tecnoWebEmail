package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.OrderDetail;
import com.example.tecnoWebEmail.Models.ProductionOrder;
import com.example.tecnoWebEmail.Repository.OrderDetailRepository; // Necesario
import com.example.tecnoWebEmail.Repository.ProductionOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ProductionOrderService {

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository; // Inyectado para buscar el detalle

    /**
     * Crea una Orden de Producción recibiendo el ID del detalle y las fechas.
     *
     * @param orderDetailId ID del detalle de pedido que se va a fabricar.
     * @param startDate     Fecha de inicio de producción.
     * @param estimatedDate Fecha estimada de finalización.
     * @return La Orden de Producción creada.
     */
    @Transactional
    public ProductionOrder createProductionOrder(Long orderDetailId, LocalDate startDate, LocalDate estimatedDate) {

        // 1. Buscar el detalle de pedido
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with id: " + orderDetailId));

        // (Opcional: Validar que no tenga ya una orden de producción)
        if (detail.getProductionOrder() != null) {
            throw new RuntimeException("Este detalle de pedido ya tiene una orden de producción asociada.");
        }

        // 2. Crear la orden de producción
        ProductionOrder productionOrder = new ProductionOrder();
        productionOrder.setOrderDetail(detail);
        productionOrder.setStatus("Pending"); // Estado inicial

        // 3. Asignar las fechas recibidas como parámetro
        productionOrder.setStartDate(startDate);
        productionOrder.setEstimatedCompletionDate(estimatedDate);

        return productionOrderRepository.save(productionOrder);
    }

    public Optional<ProductionOrder> getProductionOrderWithDetails(Long id) {
        return productionOrderRepository.findByIdWithDetails(id);
    }
}