package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.OrderDetail;
import com.example.tecnoWebEmail.Models.ProductionOrder;
import com.example.tecnoWebEmail.Repository.ProductionOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ProductionOrderService {

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    /**
     * Crea una nueva Orden de Producción basada en un Detalle de Pedido.
     * Aquí es donde se calcula y se asignan las fechas.
     *
     * @param detail El OrderDetail que acaba de ser guardado.
     */
    @Transactional
    public void createProductionOrderForDetail(OrderDetail detail) {

        ProductionOrder productionOrder = new ProductionOrder();

        // 1. Vincular la orden de producción al detalle
        productionOrder.setOrderDetail(detail);

        // 2. Asignar estado inicial
        productionOrder.setStatus("Pending"); // O "Pendiente"

        // 3. --- ¡AQUÍ ESTÁ LA LÓGICA DE FECHAS! ---
        // La producción inicia hoy
        productionOrder.setStartDate(LocalDate.now());

        // Asumimos que la empresa tarda 7 días en fabricar.
        // ESTO ES LÓGICA DE NEGOCIO y puedes cambiarlo.
        productionOrder.setEstimatedCompletionDate(LocalDate.now().plusDays(7));

        // 4. Guardar la nueva orden de producción
        productionOrderRepository.save(productionOrder);
    }
}