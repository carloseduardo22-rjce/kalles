package dev.kalles.security.service;

import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.security.entity.Account;
import dev.kalles.security.exception.AuthenticatedAccountNotFoundException;
import dev.kalles.security.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedAccountProvider {

    private final AccountRepository accountRepository;

    public Account require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AuthenticatedAccountNotFoundException();
        }
        return accountRepository
                .findByTenantIdAndEmailIgnoreCase(TenantContextHolder.requireTenantId(), authentication.getName())
                .orElseThrow(AuthenticatedAccountNotFoundException::new);
    }
}
