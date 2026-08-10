package dev.kalles.cashregister.service;

import dev.kalles.cashregister.entity.CashRegister;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CashRegisterPaymentIntegrationService {

    private final JdbcTemplate jdbcTemplate;

    public CashRegisterPaymentIntegrationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isPaymentIntegrationConfigured(CashRegister cashRegister) {
        Integer total = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1)
              FROM mercadopago_caixa
             WHERE cash_register_id = ?
               AND mp_pos_id IS NOT NULL
            """,
            Integer.class,
            cashRegister.getId()
        );

        return total != null && total > 0;
    }

    public List<UUID> listCashRegistersWithPaymentIntegration() {
        return jdbcTemplate.queryForList(
            """
            SELECT cash_register_id
              FROM mercadopago_caixa
             WHERE cash_register_id IS NOT NULL
               AND mp_pos_id IS NOT NULL
            """,
            UUID.class
        );
    }
}
