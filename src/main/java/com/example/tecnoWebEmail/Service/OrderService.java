package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.*;
import com.example.tecnoWebEmail.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Order orderData, List<OrderDetail> details, Long clientId, Long userId, Integer numberOfInstallments) {

        // 1. Validar y obtener entidades principales
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User (employee) not found with id: " + userId));

        // 2. Preparar el objeto Order
        Order order = new Order();
        order.setClient(client);
        order.setUser(user);
        order.setPaymentCondition(orderData.getPaymentCondition());
        order.setStatus("Pending");

        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<OrderDetail> processedDetails = new HashSet<>();

        // 3. Procesar cada línea de detalle (OrderDetail)
        for (OrderDetail detail : details) {
            Product product = productRepository.findById(detail.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + detail.getProduct().getId()));

            detail.setUnitPrice(product.getPrecioVenta());
            detail.setProduct(product);
            detail.setOrder(order); // Vincular el detalle al pedido

            // Sumar al total
            BigDecimal lineTotal = detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            processedDetails.add(detail);
        }

        // 4. Guardar el Pedido (Order) y sus Detalles (OrderDetails)
        order.setTotalAmount(totalAmount);
        order.setOrderDetails(processedDetails);

        // Gracias a 'cascade = CascadeType.ALL' en la entidad Order,
        // esto guarda el Order Y todos sus OrderDetails asociados.
        Order savedOrder = orderRepository.save(order);

        // 5. Lógica POST-GUARDADO (Crédito y Producción)

        // 5a. Si es a crédito, crear las cuotas
        if ("Credit".equals(savedOrder.getPaymentCondition())) {
            if (numberOfInstallments == null || numberOfInstallments <= 0) {
                throw new IllegalArgumentException("Number of installments is required for credit orders.");
            }
            // Llamar al servicio que ya creamos
            installmentService.createInstallmentsForOrder(savedOrder, numberOfInstallments);
        }

        // 5b. Crear las Órdenes de Producción (una por cada detalle)
        // for (OrderDetail detail : savedOrder.getOrderDetails()) {
        //     productionOrderService.createProductionOrderForDetail(detail);
        // }

        return savedOrder;
    }

    // --- Métodos de Consulta para Pedidos (Order) ---

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
