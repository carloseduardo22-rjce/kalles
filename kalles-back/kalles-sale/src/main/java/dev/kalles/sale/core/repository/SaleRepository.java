package dev.kalles.sale.core.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import dev.kalles.sale.core.entity.Sale;
import dev.kalles.sale.core.state.CanceledState;
import dev.kalles.sale.core.state.CompletedState;
import dev.kalles.sale.core.state.OnHoldState;
import dev.kalles.sale.core.state.SaleState;
import dev.kalles.sale.core.state.OpenState;
import dev.kalles.sale.core.state.PaidState;
import dev.kalles.sale.core.state.PaymentInProgressState;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
	Optional<Sale> findById(UUID saleId);

	@EntityGraph(attributePaths = {"items", "items.product", "payments"})
	@Query("SELECT s FROM Sale s WHERE s.sessionToken = :sessionToken AND s.state IN :states")
	Optional<Sale> findBySessionTokenAndStateIn(@Param("sessionToken") String sessionToken, @Param("states") List<SaleState> states);

	@EntityGraph(attributePaths = {"items", "items.product", "payments"})
	@Query("SELECT DISTINCT s FROM Sale s WHERE s.sessionToken = :sessionToken AND s.state IN :states")
	List<Sale> findAllBySessionTokenAndStateIn(@Param("sessionToken") String sessionToken, @Param("states") List<SaleState> states);

	default List<Sale> findCompletedBySessionToken(String sessionToken) {
		return findAllBySessionTokenAndStateIn(sessionToken, List.of(new CompletedState()));
	}

	default List<Sale> findCanceledBySessionToken(String sessionToken) {
		return findAllBySessionTokenAndStateIn(sessionToken, List.of(new CanceledState()));
	}
	
	default Optional<Sale> findActiveSaleBySessionToken(String sessionToken) {
		return findBySessionTokenAndStateIn(sessionToken, 
			List.of(new OpenState(), new OnHoldState()));
	}

	default Optional<Sale> findSaleForPaymentBySessionToken(String sessionToken) {
		return findBySessionTokenAndStateIn(sessionToken,
			List.of(new OpenState(), new PaymentInProgressState()));
	}

	default Optional<Sale> findPaidSaleBySessionToken(String sessionToken) {
		return findBySessionTokenAndStateIn(sessionToken,
			List.of(new PaidState()));
	}

	@Query(value = """
			SELECT COALESCE(SUM(s.total), 0)
			FROM sale s
			JOIN cash_register_sessions crs ON CAST(crs.id AS TEXT) = s.session_token
			WHERE s.state = 'COMPLETED'
			  AND s.company_id = :companyId
			  AND crs.opened_at >= :start
			  AND crs.opened_at < :end
			""", nativeQuery = true)
	BigDecimal sumCompletedTotalsBetween(
			@Param("companyId") UUID companyId,
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);
}
