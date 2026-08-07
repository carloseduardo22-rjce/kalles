package dev.kalles.product.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(name = "uk_product_internal_code_tenant", columnNames = { "internal_code", "tenant_id" }),
		@UniqueConstraint(name = "uk_product_barcode_tenant", columnNames = { "barcode", "tenant_id" })
})
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

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(name = "internal_code", nullable = false, length = 50)
	private String internalCode;

	@Column(length = 50)
	private String barcode;

	private String description;

}
