package dev.kalles.security.service;

import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import org.springframework.stereotype.Component;

@Component
public class PosBindingAccessPolicy {

    public boolean requiresPairedDevice(Account account) {
        return account.getRole() == AccountRole.OPERATOR;
    }
}
