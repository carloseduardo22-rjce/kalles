package dev.kalles.cashregister.service;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PermissionService - Serviço de Permissões")
class PermissionServiceTest {

    private final PermissionService permissionService = new PermissionService();

    private Operator operatorWith(PermissionLevel level) {
        Operator op = new Operator();
        op.setPermissionLevel(level);
        return op;
    }

    // --- canRemoveItens ---

    @Test
    @DisplayName("Deve permitir remoção de itens para supervisor")
    void shouldAllowItemRemovalForSupervisor() {
        assertTrue(permissionService.canRemoveItens(operatorWith(PermissionLevel.SUPERVISOR)));
    }

    @Test
    @DisplayName("Deve negar remoção de itens para operador básico")
    void shouldDenyItemRemovalForBasicOperator() {
        assertFalse(permissionService.canRemoveItens(operatorWith(PermissionLevel.BASIC)));
    }

    // --- canCancelSale ---

    @Test
    @DisplayName("Deve permitir cancelamento de venda para supervisor")
    void shouldAllowSaleCancellationForSupervisor() {
        assertTrue(permissionService.canCancelSale(operatorWith(PermissionLevel.SUPERVISOR)));
    }

    @Test
    @DisplayName("Deve negar cancelamento de venda para operador básico")
    void shouldDenySaleCancellationForBasicOperator() {
        assertFalse(permissionService.canCancelSale(operatorWith(PermissionLevel.BASIC)));
    }

    // --- canAuthorizeRemoval ---

    @Test
    @DisplayName("Deve permitir que supervisor autorize remoção para operador básico")
    void shouldAllowSupervisorToAuthorizeRemovalForBasic() {
        Operator supervisor = operatorWith(PermissionLevel.SUPERVISOR);
        Operator basic = operatorWith(PermissionLevel.BASIC);
        assertTrue(permissionService.canAuthorizeRemoval(supervisor, basic));
    }

    @Test
    @DisplayName("Deve negar que operador básico autorize remoção para supervisor")
    void shouldDenyBasicFromAuthorizingRemovalForSupervisor() {
        Operator basic = operatorWith(PermissionLevel.BASIC);
        Operator supervisor = operatorWith(PermissionLevel.SUPERVISOR);
        assertFalse(permissionService.canAuthorizeRemoval(basic, supervisor));
    }

    @Test
    @DisplayName("Deve negar que operador básico autorize remoção para outro básico")
    void shouldDenyBasicFromAuthorizingRemovalForAnotherBasic() {
        Operator basic1 = operatorWith(PermissionLevel.BASIC);
        Operator basic2 = operatorWith(PermissionLevel.BASIC);
        assertFalse(permissionService.canAuthorizeRemoval(basic1, basic2));
    }

    // --- canAuthorizeCancellation ---

    @Test
    @DisplayName("Deve permitir que supervisor autorize cancelamento para operador básico")
    void shouldAllowSupervisorToAuthorizeCancellationForBasic() {
        Operator supervisor = operatorWith(PermissionLevel.SUPERVISOR);
        Operator basic = operatorWith(PermissionLevel.BASIC);
        assertTrue(permissionService.canAuthorizeCancellation(supervisor, basic));
    }

    @Test
    @DisplayName("Deve negar que operador básico autorize cancelamento para supervisor")
    void shouldDenyBasicFromAuthorizingCancellationForSupervisor() {
        Operator basic = operatorWith(PermissionLevel.BASIC);
        Operator supervisor = operatorWith(PermissionLevel.SUPERVISOR);
        assertFalse(permissionService.canAuthorizeCancellation(basic, supervisor));
    }
}
