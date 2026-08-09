package dev.kalles.support.dto;

import dev.kalles.support.entity.AgentEntity;

import java.util.UUID;

public record AgentResponse(
    UUID id,
    String employeeId,
    String name,
    boolean active
) {
    public static AgentResponse from(AgentEntity agent) {
        return new AgentResponse(
                agent.getId(),
                agent.getEmployeeId(),
                agent.getName(),
                agent.isActive()
        );
    }
}
