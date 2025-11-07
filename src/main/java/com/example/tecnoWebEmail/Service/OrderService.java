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
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(String paymentCondition, Map<Long, Integer> productDetails, String clientCi, String userCi, Integer numberOfInstallments) {

        // 1. Validar y obtener entidades principales POR CI
        Client client = clientRepository.findByCi(clientCi)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con CI: " + clientCi));
        User user = userRepository.findByCi(userCi)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con CI: " + userCi));

        // 2. Preparar el objeto Order
        Order order = new Order();
        order.setClient(client);
        order.setUser(user);
        order.setPaymentCondition(paymentCondition);
        order.setStatus("Pending"); // Estado de producción del pedido

        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<OrderDetail> processedDetails = new HashSet<>();

        // 3. Procesar cada línea de detalle (del Mapa)
        for (Map.Entry<Long, Integer> entry : productDetails.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productId));

            // Crear el nuevo detalle
            OrderDetail detail = new OrderDetail();
            detail.setQuantity(quantity);
            detail.setUnitPrice(product.getPrecioVenta());
            detail.setProduct(product);
            detail.setOrder(order);

            // Sumar al total
            BigDecimal lineTotal = detail.getUnitPrice().multiply(new BigDecimal(quantity));
            totalAmount = totalAmount.add(lineTotal);

            processedDetails.add(detail);
        }

        if (processedDetails.isEmpty()) {
            throw new RuntimeException("No se puede crear una orden sin productos.");
        }

        // 4. Guardar el Pedido (Order) y sus Detalles (OrderDetails)
        order.setTotalAmount(totalAmount);
        order.setOrderDetails(processedDetails);

        Order savedOrder = orderRepository.save(order);

        // 5. Lógica POST-GUARDADO (Crédito y Producción)

        // 5a. Si es a crédito, crear las cuotas
        if ("Credit".equals(savedOrder.getPaymentCondition())) {
            if (numberOfInstallments == null || numberOfInstallments <= 0) {
                throw new IllegalArgumentException("Se requiere un número de cuotas > 0 para pedidos a crédito.");
            }
            installmentService.createInstallmentsForOrder(savedOrder, numberOfInstallments);
        }

        // 5b. Crear las Órdenes de Producción (una por cada detalle)
        for (OrderDetail detail : savedOrder.getOrderDetails()) {
            productionOrderService.createProductionOrderForDetail(detail);
        }

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
