package dev.kalles.sale.core.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;import jakarta.persistence.Version;import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(indexes = {
	@Index(name = "idx_product_internal_code", columnList = "internal_code"),
	@Index(name = "idx_product_barcode", columnList = "barcode")
}, 
comment = "Entidade representando um produto: código interno, código de barras, nome, descrição, preço e status de ativo/inativo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

    @Version
    private Long version;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(name = "internal_code", nullable = false, unique = true, length = 50)
	private String internalCode;

	@Column(unique = true, length = 50)
	private String barcode;

	private String description;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "stock_quantity", nullable = false)
	private int stockQuantity;

}
