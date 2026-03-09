package dev.kalles.support.application.service;

import dev.kalles.support.application.exception.NotFoundException;
import dev.kalles.support.domain.Ticket;
import dev.kalles.support.domain.Category;
import dev.kalles.support.domain.Agent;
import dev.kalles.support.domain.User;
import dev.kalles.support.domain.TicketStatus;
import dev.kalles.support.infrastructure.persistence.entity.*;
import dev.kalles.support.infrastructure.persistence.mapper.TicketMapper;
import dev.kalles.support.infrastructure.persistence.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final AgentService agentService;
    private final CategoryService categoryService;
    private final TicketMapper mapper;

    // ---------------------------------------------------------------
    // Queries
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Ticket> listAll(Optional<TicketStatus> status) {
        List<TicketEntity> entities = status
                .map(ticketRepository::findAllByStatusOrderByCreatedAtDesc)
                .orElseGet(ticketRepository::findAllByOrderByCreatedAtDesc);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public Ticket findById(UUID id) {
        TicketEntity entity = ticketRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findByAgent(UUID agentId) {
        return ticketRepository.findAllByAgentIdOrderByCreatedAtDesc(agentId)
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public List<Ticket> findByUser(UUID userId) {
        return ticketRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(mapper::toDomain).toList();
    }

    // ---------------------------------------------------------------
    // Commands
    // ---------------------------------------------------------------

    /**
     * Opens a new ticket. The user is found-or-created by email.
     * Priority is automatically derived from the category's defaultPriority.
     */
    @Transactional
    public Ticket openTicket(String title, String description,
                             String userEmail, String userName,
                             UUID categoryId) {
        UserEntity userEntity = userService.findOrCreate(userEmail, userName);
        CategoryEntity categoryEntity = categoryService.findById(categoryId);

        Category category = mapper.toDomainCategory(categoryEntity);
        User user = mapper.toDomainUser(userEntity);

        Ticket ticket = Ticket.open(title, description, user, category);

        TicketEntity entity = mapper.toEntity(ticket, userEntity, null, categoryEntity);
        ticketRepository.save(entity);

        return ticket;
    }

    /**
     * Assigns an agent to an OPEN ticket.
     * Delegates state-transition validation to the domain aggregate.
     */
    @Transactional
    public Ticket assignTicket(UUID ticketId, UUID agentId) {
        TicketEntity ticketEntity = ticketRepository.findWithDetailsById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));
        AgentEntity agentEntity = agentService.findById(agentId);

        Ticket ticket = mapper.toDomain(ticketEntity);
        Agent agent = mapper.toDomainAgent(agentEntity);

        ticket.assign(agent); // domain enforces the state transition

        ticketEntity.setStatus(ticket.getStatus());
        ticketEntity.setAgent(agentEntity);

        // Persist the new interaction added by the state transition
        ticket.getInteractions().stream()
                .filter(i -> ticketEntity.getInteractions().stream()
                        .noneMatch(e -> e.getCreatedAt().equals(i.getCreatedAt())
                                && e.getContent().equals(i.getContent())))
                .map(i -> mapper.toInteractionEntity(i, ticketEntity))
                .forEach(ticketEntity.getInteractions()::add);

        ticketRepository.save(ticketEntity);
        return mapper.toDomain(ticketEntity);
    }
}
