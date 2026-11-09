package dev.kalles.security.service;

import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.exception.AccountEmailAlreadyExistsException;
import dev.kalles.security.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperatorAccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account create(String name, String email, String rawPassword, UUID companyId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        accountRepository.findByTenantIdAndEmailIgnoreCase(tenantId, email).ifPresent(existing -> {
            throw new AccountEmailAlreadyExistsException();
        });
        Account account = new Account(tenantId, name, email, passwordEncoder.encode(rawPassword), AccountRole.OPERATOR);
        account.setCompanyId(companyId);
        account.setVerified(true);
        return accountRepository.save(account);
    }
}
