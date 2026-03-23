package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.dto.CreateCashRegisterRequest;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CashRegisterCommandService {

    private final CashRegisterRepository repository;

    @Transactional
    public CashRegister create(CreateCashRegisterRequest request) {
        if (repository.findByCode(request.code()).isPresent()) {
            throw new IllegalArgumentException("Já existe um caixa com este código: " + request.code());
        }

        CashRegister cashRegister = new CashRegister(request.code(), request.description());
        return repository.save(cashRegister);
    }
}
