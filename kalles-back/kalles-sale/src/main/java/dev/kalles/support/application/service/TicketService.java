package dev.kalles.support.application.service;

import dev.kalles.sale.security.domain.Account;
import dev.kalles.sale.security.repository.AccountRepository;
import dev.kalles.support.application.exception.NotFoundException;
import dev.kalles.support.domain.Agent;
import dev.kalles.support.domain.Category;
import dev.kalles.support.domain.Ticket;
import dev.kalles.support.domain.TicketStatus;
import dev.kalles.support.domain.User;
import dev.kalles.support.infrastructure.persistence.entity.AgentEntity;
import dev.kalles.support.infrastructure.persistence.entity.CategoryEntity;
import dev.kalles.support.infrastructure.persistence.entity.TicketEntity;
import dev.kalles.support.infrastructure.persistence.entity.UserEntity;
import dev.kalles.support.infrastructure.persistence.mapper.TicketMapper;
import dev.kalles.support.infrastructure.persistence.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    private final AccountRepository accountRepository;
    private final TicketMapper mapper;

    @Transactional(readOnly = true)
    public List<Ticket> listAccessible(Optional<TicketStatus> status, String actorEmail, boolean isAdmin) {
        List<TicketEntity> entities;
        if (isAdmin) {
            entities = status
                    .map(ticketRepository::findAllByStatusOrderByCreatedAtDesc)
                    .orElseGet(ticketRepository::findAllByOrderByCreatedAtDesc);
        } else {
            entities = status
                    .map(value -> ticketRepository.findAllByUserEmailAndStatusOrderByCreatedAtDesc(actorEmail, value))
                    .orElseGet(() -> ticketRepository.findAllByUserEmailOrderByCreatedAtDesc(actorEmail));
        }
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public Ticket findAccessibleById(UUID id, String actorEmail, boolean isAdmin) {
        TicketEntity entity = findTicketEntity(id);
        if (!isAdmin && !entity.getUser().getEmail().equalsIgnoreCase(actorEmail)) {
            throw new NotFoundException("Ticket not found: " + id);
        }
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

    @Transactional
    public Ticket openTicket(String title, String description, UUID categoryId, String actorEmail) {
        Account account = findAccount(actorEmail);
        UserEntity userEntity = userService.findOrCreate(account.getEmail(), account.getName());
        CategoryEntity categoryEntity = categoryService.findById(categoryId);

        Category category = mapper.toDomainCategory(categoryEntity);
        User user = mapper.toDomainUser(userEntity);
        Ticket ticket = Ticket.open(title, description, user, category);

        TicketEntity entity = mapper.toEntity(ticket, userEntity, null, categoryEntity);
        ticketRepository.save(entity);
        return mapper.toDomain(entity);
    }

    @Transactional
    public Ticket assignTicket(UUID ticketId, UUID agentId) {
        TicketEntity ticketEntity = findTicketEntity(ticketId);
        AgentEntity agentEntity = agentService.findById(agentId);

        Ticket ticket = mapper.toDomain(ticketEntity);
        Agent agent = mapper.toDomainAgent(agentEntity);
        ticket.assign(agent);

        ticketEntity.setAgent(agentEntity);
        return persist(ticketEntity, ticket);
    }

    @Transactional
    public Ticket addCustomerMessage(UUID ticketId, String content, String actorEmail) {
        TicketEntity ticketEntity = findTicketEntity(ticketId);
        ensureCustomerOwnsTicket(ticketEntity, actorEmail);

        Ticket ticket = mapper.toDomain(ticketEntity);
        ticket.addCustomerMessage(content);
        return persist(ticketEntity, ticket);
    }

    @Transactional
    public Ticket editLastCustomerMessage(UUID ticketId, String content, String actorEmail) {
        TicketEntity ticketEntity = findTicketEntity(ticketId);
        ensureCustomerOwnsTicket(ticketEntity, actorEmail);

        Ticket ticket = mapper.toDomain(ticketEntity);
        ticket.editLastCustomerMessage(content);
        return persist(ticketEntity, ticket);
    }

    @Transactional
    public Ticket addAgentMessage(UUID ticketId, String content, boolean markAsResolved) {
        TicketEntity ticketEntity = findTicketEntity(ticketId);
        Ticket ticket = mapper.toDomain(ticketEntity);
        ticket.addAgentMessage(content, markAsResolved);
        return persist(ticketEntity, ticket);
    }

    @Transactional
    public Ticket editLastAgentMessage(UUID ticketId, String content) {
        TicketEntity ticketEntity = findTicketEntity(ticketId);
        Ticket ticket = mapper.toDomain(ticketEntity);
        ticket.editLastAgentMessage(content);
        return persist(ticketEntity, ticket);
    }

    @Transactional
    public Ticket closeTicket(UUID ticketId) {
        TicketEntity ticketEntity = findTicketEntity(ticketId);
        Ticket ticket = mapper.toDomain(ticketEntity);
        ticket.close();
        return persist(ticketEntity, ticket);
    }

    private TicketEntity findTicketEntity(UUID ticketId) {
        return ticketRepository.findWithDetailsById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));
    }

    private Account findAccount(String actorEmail) {
        return accountRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new NotFoundException("Account not found: " + actorEmail));
    }

    private void ensureCustomerOwnsTicket(TicketEntity ticketEntity, String actorEmail) {
        if (!ticketEntity.getUser().getEmail().equalsIgnoreCase(actorEmail)) {
            throw new NotFoundException("Ticket not found: " + ticketEntity.getId());
        }
    }

    private Ticket persist(TicketEntity ticketEntity, Ticket ticket) {
        ticketEntity.setStatus(ticket.getStatus());
        ticketEntity.setSlaActive(ticket.getStatus() == TicketStatus.OPEN || ticket.getStatus() == TicketStatus.IN_PROGRESS);
        if (ticketEntity.getSlaStartedAt() == null) {
            ticketEntity.setSlaStartedAt(Instant.now());
        }

        ticketEntity.getInteractions().clear();
        ticket.getInteractions().stream()
                .map(interaction -> mapper.toInteractionEntity(interaction, ticketEntity))
                .forEach(ticketEntity.getInteractions()::add);

        ticketRepository.save(ticketEntity);
        return mapper.toDomain(ticketEntity);
    }
}
