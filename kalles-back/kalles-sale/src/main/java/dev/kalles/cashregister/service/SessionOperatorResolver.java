package dev.kalles.cashregister.service;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.cashregister.exception.OperatorAccountNotLinkedException;
import dev.kalles.cashregister.repository.OperatorRepository;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.service.AuthenticatedAccountProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class SessionOperatorResolver {

    private static final String ADMINISTRATOR_CODE_PREFIX = "adm-";

    private final OperatorRepository operatorRepository;
    private final AuthenticatedAccountProvider authenticatedAccountProvider;

    public Operator resolve(UUID companyId) {
        Account account = authenticatedAccountProvider.require();
        return operatorRepository.findByAccountIdAndCompanyId(account.getId(), companyId)
                .map(this::requireActive)
                .orElseGet(() -> enrollAdministrator(account, companyId));
    }

    private Operator requireActive(Operator operator) {
        if (!operator.isActive()) {
            throw new OperatorAccountNotLinkedException();
        }
        return operator;
    }

    private Operator enrollAdministrator(Account account, UUID companyId) {
        if (account.getRole() != AccountRole.ADMIN) {
            throw new OperatorAccountNotLinkedException();
        }
        Operator operator = new Operator(account.getName(), nextAdministratorCode(companyId));
        operator.setCompanyId(companyId);
        operator.setAccountId(account.getId());
        operator.setPermissionLevel(PermissionLevel.ADMIN);
        return operatorRepository.save(operator);
    }

    private String nextAdministratorCode(UUID companyId) {
        return IntStream.iterate(1, number -> number + 1)
                .mapToObj(number -> ADMINISTRATOR_CODE_PREFIX + number)
                .filter(code -> !operatorRepository.existsByCodeAndCompanyId(code, companyId))
                .findFirst()
                .orElseThrow();
    }
}
