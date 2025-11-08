package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.Product;
import com.example.tecnoWebEmail.Models.ProductMovement;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductCommand {

	@Autowired
	private ProductService productService;

	@Autowired
	private EmailResponseService emailResponseService;

	private static final String MINI_SEPARATOR = "------------------------";

	// LISTADO: devuelve todos los productos
	public String handleListProducts() {
		try {
			List<Product> products = productService.getAllProducts();
			return formatListProductsResponse(products, "LISPRO");
		} catch (Exception e) {
			return emailResponseService.formatErrorResponse("Error al listar productos: " + e.getMessage(), "LISPRO");
		}
	}

	// BUSPRO ["sku"]
	public String handleSearchProductBySku(String[] parameters) {
		try {
			if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
				return emailResponseService.formatErrorResponse("Parámetro [sku] faltante.", "BUSPRO");
			}
			String sku = parameters[0].trim();
			Product product = productService.getProductBySku(sku);
			if (product == null) {
				return emailResponseService.formatErrorResponse("Producto no encontrado con SKU: " + sku, "BUSPRO");
			}
			return formatSingleProductResponse(product, "PRODUCTO ENCONTRADO", "BUSPRO");
		} catch (Exception e) {
			return emailResponseService.formatErrorResponse("Error al buscar producto: " + e.getMessage(), "BUSPRO");
		}
	}

	// INSPRO ["sku","nombre","descripcion","precio","stockInicial"]
	public String handleInsertProduct(String[] parameters) {
		try {
			if (parameters.length < 5) {
				return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 5: [sku,nombre,descripcion,precio,stockInicial]", "INSPRO");
			}

			String sku = parameters[0].trim();
			String nombre = parameters[1].trim();
			String descripcion = parameters[2].trim();
			BigDecimal precio = new BigDecimal(parameters[3].trim());
			Integer stockInicial = Integer.parseInt(parameters[4].trim());

			Product p = new Product();
			p.setSku(sku);
			p.setNombre(nombre);
			p.setDescripcion(descripcion);
			p.setPrecioVenta(precio);
			p.setStockActual(stockInicial);

			Product created = productService.createProduct(p);
			return formatSingleProductResponse(created, "PRODUCTO CREADO EXITOSAMENTE", "INSPRO");
		} catch (Exception e) {
			return emailResponseService.formatErrorResponse("Error al crear producto: " + e.getMessage(), "INSPRO");
		}
	}

	// UPDPRO ["sku","nuevoNombre","nuevaDescripcion","nuevoPrecio"]
	public String handleUpdateProduct(String[] parameters) {
		try {
			if (parameters.length < 4) {
				return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 4: [sku,nombre,descripcion,precio]", "UPDPRO");
			}
			String sku = parameters[0].trim();
			Product existing = productService.getProductBySku(sku);
			if (existing == null) return emailResponseService.formatErrorResponse("Producto no encontrado con SKU: " + sku, "UPDPRO");

			existing.setNombre(parameters[1].trim());
			existing.setDescripcion(parameters[2].trim());
			existing.setPrecioVenta(new BigDecimal(parameters[3].trim()));

			Product updated = productService.updateProduct(existing);
			return formatSingleProductResponse(updated, "PRODUCTO ACTUALIZADO", "UPDPRO");
		} catch (Exception e) {
			return emailResponseService.formatErrorResponse("Error al actualizar producto: " + e.getMessage(), "UPDPRO");
		}
	}

	// ENTPRO ["productId","cantidad","motivo"] -> registrar entrada
	public String handleRegisterEntry(String[] parameters) {
		try {
			if (parameters.length < 3) {
				return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 3: [productId,cantidad,motivo]", "ENTPRO");
			}
			Long productId = Long.parseLong(parameters[0].trim());
			int qty = Integer.parseInt(parameters[1].trim());
			String reason = parameters[2].trim();

			ProductMovement mv = productService.registerProductEntry(productId, qty, reason, null);
			return emailResponseService.generateHeader("ENTPRO") + " Entrada registrada. Nuevo stock: " + mv.getProduct().getStockActual() + "\nOK\n";
		} catch (Exception e) {
			return emailResponseService.formatErrorResponse("Error al registrar entrada: " + e.getMessage(), "ENTPRO");
		}
	}

	// SALPRO ["productId","cantidad","motivo"] -> registrar salida
	public String handleRegisterExit(String[] parameters) {
		try {
			if (parameters.length < 3) {
				return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 3: [productId,cantidad,motivo]", "SALPRO");
			}
			Long productId = Long.parseLong(parameters[0].trim());
			int qty = Integer.parseInt(parameters[1].trim());
			String reason = parameters[2].trim();

			ProductMovement mv = productService.registerProductExit(productId, qty, reason, null);
			return emailResponseService.generateHeader("SALPRO") + " Salida registrada. Nuevo stock: " + mv.getProduct().getStockActual() + "\nOK\n";
		} catch (Exception e) {
			return emailResponseService.formatErrorResponse("Error al registrar salida: " + e.getMessage(), "SALPRO");
		}
	}

	// ADJPRO ["productId","nuevoStock","motivo"] -> ajuste
	public String handleAdjustStock(String[] parameters) {
		try {
			if (parameters.length < 3) {
				return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 3: [productId,nuevoStock,motivo]", "ADJPRO");
			}
			Long productId = Long.parseLong(parameters[0].trim());
			int qty = Integer.parseInt(parameters[1].trim());
			String reason = parameters[2].trim();

			ProductMovement mv = productService.adjustStock(productId, qty, reason);
			return emailResponseService.generateHeader("ADJPRO") + " Ajuste registrado. Nuevo stock: " + mv.getProduct().getStockActual() + "\nOK\n";
		} catch (Exception e) {
			return emailResponseService.formatErrorResponse("Error al ajustar stock: " + e.getMessage(), "ADJPRO");
		}
	}

	// --- Helpers de respuesta ---
	private String formatSingleProductResponse(Product product, String message, String command) {
		StringBuilder response = new StringBuilder();
		response.append(emailResponseService.generateHeader(command));
		response.append(" ").append(message).append("\n");
		response.append(MINI_SEPARATOR).append("\n");
		response.append("   • ID: ").append(product.getId()).append("\n");
		response.append("   • SKU: ").append(product.getSku()).append("\n");
		response.append("   • Nombre: ").append(product.getNombre()).append("\n");
		response.append("   • Descripción: ").append(product.getDescripcion()).append("\n");
		response.append("   • Precio Venta: ").append(product.getPrecioVenta()).append("\n");
		response.append("   • Stock Actual: ").append(product.getStockActual()).append("\n\n");
		response.append("OK\n");
		return response.toString();
	}

	private String formatListProductsResponse(List<Product> products, String command) {
		StringBuilder response = new StringBuilder();
		response.append(emailResponseService.generateHeader(command));

		if (products.isEmpty()) {
			response.append("RESULTADO DEL LISTADO\n");
			response.append(MINI_SEPARATOR).append("\n");
			response.append(" No se encontraron productos en la base de datos.\n");
		} else {
			response.append(" LISTADO DE PRODUCTOS\n");
			response.append(MINI_SEPARATOR).append("\n");
			response.append("Total de registros encontrados: ").append(products.size()).append("\n\n");

			int contador = 1;
			for (Product product : products) {
				response.append(" PRODUCTO #").append(contador).append("\n");
				response.append("   • ID: ").append(product.getId()).append("\n");
				response.append("   • SKU: ").append(product.getSku()).append("\n");
				response.append("   • Nombre: ").append(product.getNombre()).append("\n");
				response.append("   • Precio: ").append(product.getPrecioVenta()).append("\n");
				response.append("   • Stock: ").append(product.getStockActual()).append("\n\n");
				contador++;
			}
		}
		response.append("OK\n");
		return response.toString();
	}

}
