package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.*;
import com.example.tecnoWebEmail.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    // Servicios dependientes
    @Autowired
    private InstallmentService installmentService;

    @Autowired
    private ProductionOrderService productionOrderService;

    /**
     * @param paymentCondition   "Cash" o "Credit"
     * @param productDetails     Un Mapa donde K=ID de Producto, V=Cantidad
     * @param clientCi           CI del Cliente
     * @param userCi             CI del Usuario (empleado)
     * @param numberOfInstallments 0 si es "Cash", >0 si es "Credit"
     * @return El Pedido (Order) creado y guardado.
     */
    /**
     * ¡MÉTODO SIMPLIFICADO!
     * Ahora SOLO crea la cabecera del pedido, sin detalles.
     * El total se inicializa en 0.
     *
     * @param clientCi         CI del Cliente
     * @param userCi           CI del Usuario (empleado)
     * @param paymentCondition "Cash" o "Credit"
     * @return El Pedido (Order) creado, listo para recibir detalles.
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrderHeader(String clientCi, String userCi, String paymentCondition) {

        // 1. Validar y obtener entidades principales POR CI
        Client client = clientRepository.findByCi(clientCi)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con CI: " + clientCi));
        User user = userRepository.findByCi(userCi)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con CI: " + userCi));

        Order order = new Order();
        order.setClient(client);
        order.setUser(user);
        order.setPaymentCondition(paymentCondition);

        order.setStatus("Draft");
        order.setTotalAmount(BigDecimal.ZERO);
        order.setPaymentState("Pending");
        return orderRepository.save(order);
    }

    /**
     * ¡NUEVO MÉTODO!
     * Es llamado por OrderDetailService cada vez que se añade/elimina un detalle.
     * Recalcula el monto total del pedido sumando sus detalles.
     */
    @Transactional
    public void recalculateOrderTotal(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Obtener todos los detalles de este pedido
        List<OrderDetail> details = orderDetailRepository.findByOrder(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderDetail detail : details) {
            BigDecimal lineTotal = detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
    }

    /**
     * ¡NUEVO MÉTODO!
     * Confirma el pedido, cambia el estado y crea las cuotas si es a crédito.
     * Se debe llamar DESPUÉS de añadir todos los detalles.
     */
    @Transactional
    public Order confirmOrderAndCreateInstallments(Long orderId, Integer numberOfInstallments) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (!"Draft".equals(order.getStatus())) {
            throw new RuntimeException("La orden ya ha sido confirmada.");
        }

        // 5a. Si es a crédito, crear las cuotas
        if ("Credit".equals(order.getPaymentCondition())) {
            if (numberOfInstallments == null || numberOfInstallments <= 0) {
                throw new IllegalArgumentException("Se requiere un número de cuotas > 0 para pedidos a crédito.");
            }
            if (order.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
                throw new RuntimeException("No se pueden crear cuotas para un pedido con total 0.");
            }
            installmentService.createInstallmentsForOrder(order, numberOfInstallments);
        }

        // 5b. Cambiar el estado del pedido de "Borrador" a "Pendiente" (en producción)
        order.setStatus("Pending");
        return orderRepository.save(order);
    }

    // --- Métodos de Consulta para Pedidos (Order) ---

    public Optional<Order> getOrderByIdWithDetails(Long orderId) {
        return orderRepository.findByIdWithAllDetails(orderId);
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));
        return orderRepository.findByClient(client);
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByPaymentState(String paymentState) {
        return orderRepository.findByPaymentState(paymentState);
    }

    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    // --- Métodos de Consulta para Detalles (OrderDetail) ---

    /**
     * Obtiene los detalles de un pedido específico.
     */
    public List<OrderDetail> getOrderDetailsByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return orderDetailRepository.findByOrder(order);
    }

}
