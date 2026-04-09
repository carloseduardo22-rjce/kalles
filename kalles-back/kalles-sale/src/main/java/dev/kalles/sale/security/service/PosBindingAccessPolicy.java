package dev.kalles.sale.security.service;

import dev.kalles.sale.security.domain.Account;
import dev.kalles.sale.security.domain.AccountRole;
import org.springframework.stereotype.Component;

@Component
public class PosBindingAccessPolicy {

    public boolean requiresPairedDevice(Account account) {
        return account.getRole() == AccountRole.OPERATOR;
    }
}
