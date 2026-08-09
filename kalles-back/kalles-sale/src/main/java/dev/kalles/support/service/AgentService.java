package dev.kalles.support.service;

import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.support.entity.AgentEntity;
import dev.kalles.support.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    @Transactional(readOnly = true)
    public List<AgentEntity> listAllActive() {
        return agentRepository.findAllByTenantIdAndActiveTrueOrderByNameAsc(currentTenantId());
    }

    @Transactional(readOnly = true)
    public AgentEntity findById(UUID id) {
        return agentRepository.findByIdAndTenantId(id, currentTenantId())
                .orElseThrow(() -> new NotFoundException("Agent not found: " + id));
    }

    @Transactional
    public AgentEntity create(String employeeId, String name) {
        UUID tenantId = currentTenantId();
        agentRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).ifPresent(existing -> {
            throw new IllegalArgumentException("An agent with this employee ID already exists: " + employeeId);
        });
        AgentEntity agent = new AgentEntity();
        agent.setTenantId(tenantId);
        agent.setEmployeeId(employeeId);
        agent.setName(name);
        agent.setActive(true);
        return agentRepository.save(agent);
    }

    @Transactional
    public AgentEntity update(UUID id, String employeeId, String name) {
        AgentEntity agent = findById(id);
        UUID tenantId = currentTenantId();
        agentRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Employee ID already used by another agent: " + employeeId);
            }
        });
        agent.setEmployeeId(employeeId);
        agent.setName(name);
        return agentRepository.save(agent);
    }

    @Transactional
    public void deactivate(UUID id) {
        AgentEntity agent = findById(id);
        agent.setActive(false);
        agentRepository.save(agent);
    }

    private UUID currentTenantId() {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is required for support agents");
        }
        return tenantId;
    }
}
