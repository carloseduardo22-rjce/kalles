package dev.kalles.sale.security.dto;

import dev.kalles.sale.security.domain.Account;

public record AuthMeResponse(
        String accountId,
        String tenantId,
        String companyId,
        String email,
        String name,
        String role
) {
    public static AuthMeResponse from(Account account) {
        return new AuthMeResponse(
                account.getId().toString(),
                account.getTenantId().toString(),
                account.getCompanyId() != null ? account.getCompanyId().toString() : null,
                account.getEmail(),
                account.getName(),
                account.getRole().name()
        );
    }
}
