package dev.kalles.core.service;

import org.springframework.stereotype.Service;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.core.enums.operator.PermissionLevel;

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

    // Desconto manual é tão sensível quanto remover item: sem controle, um
    // operador poderia zerar o valor de qualquer produto.
    public boolean canApplyItemDiscount(Operator operator) {
        return operator.getPermissionLevel().getLevel() >= PermissionLevel.SUPERVISOR.getLevel();
    }

    public boolean canAuthorizeItemDiscount(Operator authorizer, Operator requester) {
        return authorizer.getPermissionLevel().canAuthorize(requester.getPermissionLevel());
    }

}
