package com.example.tecnoWebEmail.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "supplies")
public class Supply {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "supply_id")
	private Long id;

	@Column(name = "name", length = 100, nullable = false)
	private String nombre;

	@Column(name = "description", columnDefinition = "TEXT")
	private String descripcion;

	@Column(name = "unit_measure", length = 20)
	private String unidadMedida;

	@Column(name = "stock_current", precision = 10, scale = 2, nullable = false)
	private BigDecimal stockActual = BigDecimal.valueOf(0.00);

	// Constructor vacío
	public Supply() {}

	// Constructor con parámetros (sin id)
	public Supply(String nombre, String descripcion, String unidadMedida, BigDecimal stockActual) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.unidadMedida = unidadMedida;
		this.stockActual = stockActual == null ? BigDecimal.valueOf(0.00) : stockActual;
	}

	// Getters y Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public BigDecimal getStockActual() {
		return stockActual;
	}

	public void setStockActual(BigDecimal stockActual) {
		this.stockActual = stockActual == null ? BigDecimal.valueOf(0.00) : stockActual;
	}

	@Override
	public String toString() {
		return "Supply{" +
				"id=" + id +
				", nombre='" + nombre + '\'' +
				", unidadMedida='" + unidadMedida + '\'' +
				", stockActual=" + stockActual +
				'}';
	}
}
