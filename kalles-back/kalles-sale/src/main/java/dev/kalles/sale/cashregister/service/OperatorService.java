package dev.kalles.sale.cashregister.service;

import dev.kalles.sale.cashregister.dto.OperatorRequest;
import dev.kalles.sale.cashregister.dto.OperatorResponse;
import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.core.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperatorService {

    private final OperatorRepository operatorRepository;

    @Transactional(readOnly = true)
    public List<OperatorResponse> listAll() {
        return operatorRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(OperatorResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public OperatorResponse findById(UUID id) {
        return operatorRepository.findById(id)
                .map(OperatorResponse::fromEntity)
                .orElseThrow(() -> new NotFoundException("Operador não encontrado: " + id));
    }

    @Transactional
    public OperatorResponse create(OperatorRequest request) {
        operatorRepository.findByCode(request.code()).ifPresent(existing -> {
            throw new IllegalArgumentException("Já existe um operador com o código informado.");
        });
        Operator operator = new Operator();
        operator.setName(request.name());
        operator.setCode(request.code());
        operator.setPermissionLevel(request.permissionLevel());
        operator.setActive(true);
        return OperatorResponse.fromEntity(operatorRepository.save(operator));
    }

    @Transactional
    public OperatorResponse update(UUID id, OperatorRequest request) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Operador não encontrado: " + id));
        operatorRepository.findByCode(request.code()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Já existe um operador com o código informado.");
            }
        });
        operator.setName(request.name());
        operator.setCode(request.code());
        operator.setPermissionLevel(request.permissionLevel());
        return OperatorResponse.fromEntity(operatorRepository.save(operator));
    }

    @Transactional
    public void deactivate(UUID id) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Operador não encontrado: " + id));
        operator.setActive(false);
        operatorRepository.save(operator);
    }
}
