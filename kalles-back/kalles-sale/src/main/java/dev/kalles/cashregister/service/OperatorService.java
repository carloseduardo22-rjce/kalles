package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.CreateOperatorRequest;
import dev.kalles.cashregister.dto.OperatorRequest;
import dev.kalles.cashregister.dto.OperatorResponse;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.exception.OperatorCodeAlreadyExistsException;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.entity.Account;
import dev.kalles.security.service.OperatorAccountService;
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
    private final OperatorAccountService operatorAccountService;

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
    public OperatorResponse create(CreateOperatorRequest request) {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        operatorRepository.findByCodeAndCompanyId(request.code(), companyId).ifPresent(existing -> {
            throw new OperatorCodeAlreadyExistsException();
        });
        Account account = operatorAccountService.create(request.name(), request.email(), request.password(), companyId);
        Operator operator = new Operator();
        operator.setCompanyId(companyId);
        operator.setAccountId(account.getId());
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
                throw new OperatorCodeAlreadyExistsException();
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
