package com.example.tecnoWebEmail.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Long id;

	@Column(name = "sku", length = 50, nullable = false, unique = true)
	private String sku; // Código de producto

	@Column(name = "name", length = 100, nullable = false)
	private String nombre;

	@Column(name = "description", columnDefinition = "TEXT")
	private String descripcion;

	@Column(name = "sale_price", precision = 10, scale = 2, nullable = false)
	private BigDecimal precioVenta;

	@Column(name = "stock_current", nullable = false)
	private Integer stockActual = 0;

	// Constructor vacío
	public Product() {}

	// Constructor con parámetros (sin id)
	public Product(String sku, String nombre, String descripcion, BigDecimal precioVenta, Integer stockActual) {
		this.sku = sku;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precioVenta = precioVenta;
		this.stockActual = stockActual == null ? 0 : stockActual;
	}

	// Getters y Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getPrecioVenta() {
		return precioVenta;
	}

	public void setPrecioVenta(BigDecimal precioVenta) {
		this.precioVenta = precioVenta;
	}

	public Integer getStockActual() {
		return stockActual;
	}

	public void setStockActual(Integer stockActual) {
		this.stockActual = stockActual == null ? 0 : stockActual;
	}

	@Override
	public String toString() {
		return "Product{" +
				"id=" + id +
				", sku='" + sku + '\'' +
				", nombre='" + nombre + '\'' +
				", precioVenta=" + precioVenta +
				", stockActual=" + stockActual +
				'}';
	}
}
