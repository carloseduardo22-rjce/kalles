package dev.kalles.support.infrastructure.persistence.mapper;

import dev.kalles.support.domain.*;
import dev.kalles.support.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TicketMapper {

    // ---------------------------------------------------------------
    // Domain → Entity
    // ---------------------------------------------------------------

    public TicketEntity toEntity(Ticket ticket,
                                 UserEntity userEntity,
                                 AgentEntity agentEntity,
                                 CategoryEntity categoryEntity) {
        TicketEntity entity = new TicketEntity();
        entity.setId(UUID.fromString(ticket.getId()));
        entity.setTitle(ticket.getTitle());
        entity.setDescription(ticket.getDescription());
        entity.setStatus(ticket.getStatus());
        entity.setPriority(ticket.getPriority());
        entity.setUser(userEntity);
        entity.setAgent(agentEntity);
        entity.setCategory(categoryEntity);
        entity.setSlaActive(ticket.getSla().isActive());
        entity.setSlaStartedAt(ticket.getSla().getStartedAt());
        return entity;
    }

    public InteractionEntity toInteractionEntity(Interaction interaction, TicketEntity ticketEntity) {
        InteractionEntity entity = new InteractionEntity();
        entity.setId(UUID.fromString(interaction.getId()));
        entity.setTicket(ticketEntity);
        entity.setContent(interaction.getContent());
        entity.setType(interaction.getType());
        entity.setCreatedAt(interaction.getCreatedAt());
        return entity;
    }

    // ---------------------------------------------------------------
    // Entity → Domain
    // ---------------------------------------------------------------

    public Ticket toDomain(TicketEntity entity) {
        User user = toDomainUser(entity.getUser());
        Agent agent = entity.getAgent() != null ? toDomainAgent(entity.getAgent()) : null;
        Category category = toDomainCategory(entity.getCategory());
        Sla sla = Sla.reconstitute(entity.isSlaActive(), entity.getSlaStartedAt());

        List<Interaction> interactions = entity.getInteractions()
                .stream()
                .map(this::toDomainInteraction)
                .toList();

        return Ticket.reconstitute(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getPriority(),
                user,
                agent,
                category,
                sla,
                interactions
        );
    }

    public User toDomainUser(UserEntity entity) {
        return new User(entity.getEmail(), entity.getName());
    }

    public Agent toDomainAgent(AgentEntity entity) {
        return new Agent(entity.getEmployeeId(), entity.getName());
    }

    public Category toDomainCategory(CategoryEntity entity) {
        return new Category(entity.getName(), entity.getSubcategory(), entity.getDefaultPriority());
    }

    public Interaction toDomainInteraction(InteractionEntity entity) {
        return Interaction.reconstitute(
                entity.getId().toString(),
                entity.getContent(),
                entity.getType(),
                entity.getCreatedAt()
        );
    }
}
