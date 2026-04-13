package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.dto.CreateCashRegisterRequest;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.security.context.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashRegisterCommandService {

    private final CashRegisterRepository repository;

    @Transactional
    public CashRegister create(CreateCashRegisterRequest request) {
        UUID companyId = resolveCompanyId(request.companyId());

        if (repository.findByCodeAndCompanyId(request.code(), companyId).isPresent()) {
            throw new IllegalArgumentException("Já existe um caixa com este código nesta filial: " + request.code());
        }

        CashRegister cashRegister = new CashRegister(request.code(), request.description(), companyId);
        return repository.save(cashRegister);
    }

    /**
     * Resolves the company ID with priority:
     * 1. CompanyContextHolder (secure, from JWT)
     * 2. Request parameter (backward compatibility with existing frontend)
     * 3. Throws exception if neither is available
     */
    private UUID resolveCompanyId(UUID requestCompanyId) {
        UUID contextCompanyId = CompanyContextHolder.getCompanyId();
        if (contextCompanyId != null) {
            return contextCompanyId;
        }
        if (requestCompanyId != null) {
            return requestCompanyId;
        }
        throw new IllegalStateException("Nenhuma filial selecionada no contexto da operação.");
    }
}
