package dev.kalles.sale.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.kalles.sale.dto.SessionPaymentMethodTotal;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.state.*;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
	Optional<Sale> findById(UUID saleId);

	interface SaleHistoryRow {
		String getId();
		LocalDateTime getOpenedAt();
	}

	@EntityGraph(attributePaths = {"client", "items", "items.product", "payments"})
	List<Sale> findAllByIdIn(List<UUID> ids);

	@EntityGraph(attributePaths = {"items", "items.product", "payments"})
	Optional<Sale> findBySessionIdAndStateIn(UUID sessionId, List<SaleState> states);

	@EntityGraph(attributePaths = {"items", "items.product", "payments"})
	List<Sale> findAllBySessionIdAndStateIn(UUID sessionId, List<SaleState> states);

	long countBySessionIdAndStateIn(UUID sessionId, List<SaleState> states);

	default long countCompletedBySessionId(UUID sessionId) {
		return countBySessionIdAndStateIn(sessionId, List.of(new CompletedState()));
	}

	default long countCanceledBySessionId(UUID sessionId) {
		return countBySessionIdAndStateIn(sessionId, List.of(new CanceledState()));
	}

	@Query("""
			SELECT COALESCE(SUM(s.total), 0)
			FROM Sale s
			WHERE s.sessionId = :sessionId
			  AND s.state = :state
			""")
	BigDecimal sumTotalBySessionIdAndState(
			@Param("sessionId") UUID sessionId,
			@Param("state") SaleState state);

	default BigDecimal sumCompletedTotalBySessionId(UUID sessionId) {
		return sumTotalBySessionIdAndState(sessionId, new CompletedState());
	}

	@Query("""
			SELECT new dev.kalles.sale.dto.SessionPaymentMethodTotal(
			       p.method,
			       SUM(p.amount - p.changeAmount))
			FROM Payment p
			WHERE p.sale.sessionId = :sessionId
			  AND p.sale.state = :state
			  AND p.confirmed = TRUE
			GROUP BY p.method
			""")
	List<SessionPaymentMethodTotal> sumConfirmedPaymentsByMethod(
			@Param("sessionId") UUID sessionId,
			@Param("state") SaleState state);

	default List<SessionPaymentMethodTotal> sumCompletedPaymentsByMethod(UUID sessionId) {
		return sumConfirmedPaymentsByMethod(sessionId, new CompletedState());
	}

	default Optional<Sale> findActiveSaleBySessionId(UUID sessionId) {
		return findBySessionIdAndStateIn(sessionId,
			List.of(new OpenState()));
	}

	/**
	 * Estados em que uma venda ainda pode ser cancelada: inclui vendas com
	 * pagamento em andamento (cartão recusado, cliente desistiu) e pagas mas
	 * não concluídas. Vendas COMPLETED exigem fluxo de devolução, não cancelamento.
	 */
	default Optional<Sale> findCancellableSaleBySessionId(UUID sessionId) {
		return findBySessionIdAndStateIn(sessionId,
			List.of(new OpenState(), new PaymentInProgressState(), new PaidState()));
	}

	default Optional<Sale> findSaleForPaymentBySessionId(UUID sessionId) {
		return findBySessionIdAndStateIn(sessionId,
			List.of(new OpenState(), new PaymentInProgressState()));
	}

	default Optional<Sale> findPaidSaleBySessionId(UUID sessionId) {
		return findBySessionIdAndStateIn(sessionId,
			List.of(new PaidState()));
	}

	/**
	 * Vendas que impedem o fechamento da sessão de caixa: ainda em andamento,
	 * com pagamento em curso ou pagas mas não concluídas (dinheiro recebido
	 * que não entraria no fechamento).
	 */
	default List<Sale> findPendingBySessionId(UUID sessionId) {
		return findAllBySessionIdAndStateIn(sessionId,
			List.of(new OpenState(), new PaymentInProgressState(), new PaidState()));
	}

	// Data da venda: usa o timestamp da própria venda (conclusão, com fallback
	// para criação e, por fim, abertura da sessão para linhas antigas sem backfill).
	// Antes, tudo era atribuído à data de ABERTURA da sessão — sessões virando a
	// meia-noite jogavam o faturamento no dia errado.
	@Query(value = """
			SELECT COALESCE(SUM(s.total), 0)
			FROM sale s
			LEFT JOIN cash_register_sessions crs ON crs.id = s.session_id
			WHERE s.state = 'COMPLETED'
			  AND s.company_id = :companyId
			  AND COALESCE(s.completed_at, s.created_at, crs.opened_at) >= :start
			  AND COALESCE(s.completed_at, s.created_at, crs.opened_at) < :end
			""", nativeQuery = true)
	BigDecimal sumCompletedTotalsBetween(
			@Param("companyId") UUID companyId,
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	@Query(value = """
			SELECT CAST(s.id AS VARCHAR) AS id,
			       COALESCE(s.completed_at, s.created_at, crs.opened_at) AS openedAt
			FROM sale s
			JOIN cash_register_sessions crs ON crs.id = s.session_id
			JOIN cash_registers cr ON cr.id = crs.cash_register_id
			WHERE s.company_id = :companyId
			  AND cr.company_id = :companyId
			  AND COALESCE(s.completed_at, s.created_at, crs.opened_at) >= :start
			  AND COALESCE(s.completed_at, s.created_at, crs.opened_at) < :end
			ORDER BY COALESCE(s.completed_at, s.created_at, crs.opened_at) DESC, s.id DESC
			""", nativeQuery = true)
	List<SaleHistoryRow> findHistoryRows(
			@Param("companyId") UUID companyId,
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	@Query(value = """
			SELECT CAST(s.id AS VARCHAR) AS id,
			       COALESCE(s.completed_at, s.created_at, crs.opened_at) AS openedAt
			FROM sale s
			JOIN cash_register_sessions crs ON crs.id = s.session_id
			JOIN cash_registers cr ON cr.id = crs.cash_register_id
			WHERE s.company_id = :companyId
			  AND cr.company_id = :companyId
			  AND COALESCE(s.completed_at, s.created_at, crs.opened_at) >= :start
			  AND COALESCE(s.completed_at, s.created_at, crs.opened_at) < :end
			  AND s.state = :state
			ORDER BY COALESCE(s.completed_at, s.created_at, crs.opened_at) DESC, s.id DESC
			""", nativeQuery = true)
	List<SaleHistoryRow> findHistoryRowsByState(
			@Param("companyId") UUID companyId,
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end,
			@Param("state") String state);
}
