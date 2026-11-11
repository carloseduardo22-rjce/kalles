package dev.kalles.cashregister.service;

import dev.kalles.cashregister.dto.CreateCashRegisterRequest;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.exception.CashRegisterCodeAlreadyExistsException;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.exception.CompanyContextRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CashRegisterCommandService {

    private static final String CODE_PREFIX = "CAIXA-";
    private static final Pattern SEQUENTIAL_CODE = Pattern.compile(CODE_PREFIX + "(\\d{1,9})");

    private final CashRegisterRepository repository;

    @Transactional
    public CashRegister create(CreateCashRegisterRequest request) {
        UUID companyId = resolveCompanyId(request.companyId());
        String code = nextCode(companyId);
        try {
            return repository.saveAndFlush(new CashRegister(code, request.description(), companyId));
        } catch (DataIntegrityViolationException e) {
            throw new CashRegisterCodeAlreadyExistsException(code);
        }
    }

    private String nextCode(UUID companyId) {
        int lastNumber = repository.findCodesByCompanyIdAndCodeLike(companyId, CODE_PREFIX + "%").stream()
                .map(SEQUENTIAL_CODE::matcher)
                .filter(Matcher::matches)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                .max()
                .orElse(0);
        return CODE_PREFIX + String.format("%02d", lastNumber + 1);
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
        throw new CompanyContextRequiredException();
    }
}
