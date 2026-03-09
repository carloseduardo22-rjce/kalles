package dev.kalles.support.application.dto;

import dev.kalles.support.infrastructure.persistence.entity.AgentEntity;

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
