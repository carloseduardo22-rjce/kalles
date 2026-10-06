package dev.kalles.support.exception;

import dev.kalles.shared.exception.ConflictException;

public class AgentEmployeeIdAlreadyExistsException extends ConflictException {

    public AgentEmployeeIdAlreadyExistsException(String employeeId) {
        super("SUPPORT_AGENT_EMPLOYEE_ID_ALREADY_EXISTS", "Employee ID already used by another agent: " + employeeId);
    }
}
