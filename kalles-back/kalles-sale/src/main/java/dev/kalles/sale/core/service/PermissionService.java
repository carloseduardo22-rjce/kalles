package dev.kalles.sale.core.service;

import org.springframework.stereotype.Service;

import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.core.enums.operator.PermissionLevel;

@Service
public class PermissionService {
    
    public boolean canRemoveItens(Operator operator) {
        return operator.getPermissionLevel().getLevel() >= PermissionLevel.SUPERVISOR.getLevel(); 
    }

    public boolean canAuthorizeRemoval(Operator authorizer, Operator requester) {
        return authorizer.getPermissionLevel().canAuthorize(requester.getPermissionLevel());
    }

    public boolean canCancelSale(Operator operator) {
        return operator.getPermissionLevel().getLevel() >= PermissionLevel.SUPERVISOR.getLevel();
    }

    public boolean canAuthorizeCancellation(Operator authorizer, Operator requester) {
        return authorizer.getPermissionLevel().canAuthorize(requester.getPermissionLevel());
    }

}
