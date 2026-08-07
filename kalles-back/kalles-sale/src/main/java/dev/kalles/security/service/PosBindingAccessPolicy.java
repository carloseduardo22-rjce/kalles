package dev.kalles.security.service;

import dev.kalles.security.domain.Account;
import dev.kalles.security.domain.AccountRole;
import org.springframework.stereotype.Component;

@Component
public class PosBindingAccessPolicy {

    public boolean requiresPairedDevice(Account account) {
        return account.getRole() == AccountRole.OPERATOR;
    }
}
