package dev.kalles.support.service;

import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.security.entity.Account;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.support.domain.Agent;
import dev.kalles.support.domain.Category;
import dev.kalles.support.domain.Ticket;
import dev.kalles.support.domain.TicketStatus;
import dev.kalles.support.domain.User;
import dev.kalles.support.entity.AgentEntity;
import dev.kalles.support.entity.CategoryEntity;
import dev.kalles.support.entity.TicketEntity;
import dev.kalles.support.entity.UserEntity;
import dev.kalles.support.mapper.TicketMapper;
import dev.kalles.support.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        UUID tenantId = TenantContextHolder.requireTenantId();
        List<TicketEntity> entities;
        if (isAdmin) {
            entities = status
                    .map(value -> ticketRepository.findAllByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, value))
                    .orElseGet(() -> ticketRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId));
        } else {
            entities = status
                    .map(value -> ticketRepository.findAllByTenantIdAndUserEmailIgnoreCaseAndStatusOrderByCreatedAtDesc(tenantId, actorEmail, value))
                    .orElseGet(() -> ticketRepository.findAllByTenantIdAndUserEmailIgnoreCaseOrderByCreatedAtDesc(tenantId, actorEmail));
        }
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public Page<Ticket> listAccessiblePage(
            Optional<TicketStatus> status,
            String actorEmail,
            boolean isAdmin,
            int page,
            int size
    ) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<TicketEntity> entities;
        if (isAdmin) {
            entities = status
                    .map(value -> ticketRepository.findAllByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, value, pageRequest))
                    .orElseGet(() -> ticketRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId, pageRequest));
        } else {
            entities = status
                    .map(value -> ticketRepository.findAllByTenantIdAndUserEmailIgnoreCaseAndStatusOrderByCreatedAtDesc(tenantId, actorEmail, value, pageRequest))
                    .orElseGet(() -> ticketRepository.findAllByTenantIdAndUserEmailIgnoreCaseOrderByCreatedAtDesc(tenantId, actorEmail, pageRequest));
        }
        return entities.map(mapper::toDomain);
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
        return ticketRepository.findAllByTenantIdAndAgentIdOrderByCreatedAtDesc(TenantContextHolder.requireTenantId(), agentId)
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public List<Ticket> findByUser(UUID userId) {
        return ticketRepository.findAllByTenantIdAndUserIdOrderByCreatedAtDesc(TenantContextHolder.requireTenantId(), userId)
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
        entity.setTenantId(account.getTenantId());
        entity.setCompanyId(CompanyContextHolder.getCompanyId());
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
        return ticketRepository.findWithDetailsByIdAndTenantId(ticketId, TenantContextHolder.requireTenantId())
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));
    }

    private Account findAccount(String actorEmail) {
        return accountRepository.findByTenantIdAndEmailIgnoreCase(TenantContextHolder.requireTenantId(), actorEmail)
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
