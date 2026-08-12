package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.OperatorRequest;
import dev.kalles.cashregister.dto.OperatorResponse;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.shared.exception.NotFoundException;
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
        return operatorRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(CompanyContextHolder.requireCompanyId())
                .stream()
                .map(OperatorResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public OperatorResponse findById(UUID id) {
        return operatorRepository.findByIdAndCompanyId(id, CompanyContextHolder.requireCompanyId())
                .map(OperatorResponse::fromEntity)
                .orElseThrow(() -> new NotFoundException("Operador não encontrado: " + id));
    }

    @Transactional
    public OperatorResponse create(OperatorRequest request) {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        operatorRepository.findByCodeAndCompanyId(request.code(), companyId).ifPresent(existing -> {
            throw new IllegalArgumentException("Já existe um operador com o código informado nesta filial.");
        });
        Operator operator = new Operator();
        operator.setCompanyId(companyId);
        operator.setName(request.name());
        operator.setCode(request.code());
        operator.setPermissionLevel(request.permissionLevel());
        operator.setActive(true);
        return OperatorResponse.fromEntity(operatorRepository.save(operator));
    }

    @Transactional
    public OperatorResponse update(UUID id, OperatorRequest request) {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        Operator operator = operatorRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Operador não encontrado: " + id));
        operatorRepository.findByCodeAndCompanyId(request.code(), companyId).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Já existe um operador com o código informado nesta filial.");
            }
        });
        operator.setName(request.name());
        operator.setCode(request.code());
        operator.setPermissionLevel(request.permissionLevel());
        return OperatorResponse.fromEntity(operatorRepository.save(operator));
    }

    @Transactional
    public void deactivate(UUID id) {
        Operator operator = operatorRepository.findByIdAndCompanyId(id, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Operador não encontrado: " + id));
        operator.setActive(false);
        operatorRepository.save(operator);
    }
}
