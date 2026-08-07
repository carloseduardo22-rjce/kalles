package dev.kalles.inventory.entity;

import dev.kalles.core.entity.BaseAuditableEntity;
import dev.kalles.core.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
    name = "stock_entries",
    indexes = {
        @Index(name = "idx_stock_entries_company_id", columnList = "company_id"),
        @Index(name = "idx_stock_entries_created_at", columnList = "created_at"),
        @Index(name = "idx_stock_entries_product_id", columnList = "product_id")
    },
    comment = "Historico financeiro das entradas de mercadorias no estoque"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockEntry extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "quantity_added", nullable = false)
    private int quantityAdded;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCost;
}
