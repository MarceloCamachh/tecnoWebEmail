package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.Order;
import com.example.tecnoWebEmail.Models.OrderDetail;
import com.example.tecnoWebEmail.Models.Product;
import com.example.tecnoWebEmail.Repository.OrderDetailRepository;
import com.example.tecnoWebEmail.Repository.OrderRepository;
import com.example.tecnoWebEmail.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService; // Para llamar a 'recalculateOrderTotal'

    /**
     * Añade una línea de producto (un detalle) a un pedido existente.
     *
     * @param orderId   ID del pedido (ya debe existir)
     * @param productId ID del producto a añadir
     * @param quantity  Cantidad de ese producto
     * @return El detalle (OrderDetail) que se acaba de crear.
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDetail addDetailToOrder(Long orderId, Long productId, Integer quantity) {

        // 1. Validar Pedido y Producto
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (!"Draft".equals(order.getStatus())) {
            throw new RuntimeException("No se pueden añadir detalles a un pedido que ya está confirmado.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad (quantity) debe ser mayor que 0.");
        }

        // 2. Crear el nuevo Detalle de Pedido
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setProduct(product);
        detail.setQuantity(quantity);
        detail.setUnitPrice(product.getPrecioVenta());

        // 3. Guardar el detalle
        OrderDetail savedDetail = orderDetailRepository.save(detail);

        orderService.recalculateOrderTotal(orderId);

        return savedDetail;
    }

}