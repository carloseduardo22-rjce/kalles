package dev.kalles.sale.core.service;

import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.core.enums.operator.PermissionLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionServiceTest {

    private final PermissionService permissionService = new PermissionService();

    private Operator operatorWith(PermissionLevel level) {
        Operator op = new Operator();
        op.setPermissionLevel(level);
        return op;
    }

    // --- canRemoveItens ---

    @Test
    void shouldAllowItemRemovalForSupervisor() {
        assertTrue(permissionService.canRemoveItens(operatorWith(PermissionLevel.SUPERVISOR)));
    }

    @Test
    void shouldDenyItemRemovalForBasicOperator() {
        assertFalse(permissionService.canRemoveItens(operatorWith(PermissionLevel.BASIC)));
    }

    // --- canCancelSale ---

    @Test
    void shouldAllowSaleCancellationForSupervisor() {
        assertTrue(permissionService.canCancelSale(operatorWith(PermissionLevel.SUPERVISOR)));
    }

    @Test
    void shouldDenySaleCancellationForBasicOperator() {
        assertFalse(permissionService.canCancelSale(operatorWith(PermissionLevel.BASIC)));
    }

    // --- canAuthorizeRemoval ---

    @Test
    void shouldAllowSupervisorToAuthorizeRemovalForBasic() {
        Operator supervisor = operatorWith(PermissionLevel.SUPERVISOR);
        Operator basic = operatorWith(PermissionLevel.BASIC);
        assertTrue(permissionService.canAuthorizeRemoval(supervisor, basic));
    }

    @Test
    void shouldDenyBasicFromAuthorizingRemovalForSupervisor() {
        Operator basic = operatorWith(PermissionLevel.BASIC);
        Operator supervisor = operatorWith(PermissionLevel.SUPERVISOR);
        assertFalse(permissionService.canAuthorizeRemoval(basic, supervisor));
    }

    @Test
    void shouldDenyBasicFromAuthorizingRemovalForAnotherBasic() {
        Operator basic1 = operatorWith(PermissionLevel.BASIC);
        Operator basic2 = operatorWith(PermissionLevel.BASIC);
        assertFalse(permissionService.canAuthorizeRemoval(basic1, basic2));
    }

    // --- canAuthorizeCancellation ---

    @Test
    void shouldAllowSupervisorToAuthorizeCancellationForBasic() {
        Operator supervisor = operatorWith(PermissionLevel.SUPERVISOR);
        Operator basic = operatorWith(PermissionLevel.BASIC);
        assertTrue(permissionService.canAuthorizeCancellation(supervisor, basic));
    }

    @Test
    void shouldDenyBasicFromAuthorizingCancellationForSupervisor() {
        Operator basic = operatorWith(PermissionLevel.BASIC);
        Operator supervisor = operatorWith(PermissionLevel.SUPERVISOR);
        assertFalse(permissionService.canAuthorizeCancellation(basic, supervisor));
    }
}
