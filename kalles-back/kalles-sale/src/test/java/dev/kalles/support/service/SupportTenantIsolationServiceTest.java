package dev.kalles.support.service;

import dev.kalles.support.mapper.TicketMapper;
import dev.kalles.support.repository.TicketRepository;
import dev.kalles.support.repository.UserRepository;
import dev.kalles.testsupport.RequestContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Support tenant isolation")
class SupportTenantIsolationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserService userService;

    @Mock
    private AgentService agentService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private dev.kalles.security.repository.AccountRepository accountRepository;

    @Mock
    private TicketMapper mapper;

    private final UUID tenantId = UUID.randomUUID();

    @RegisterExtension
    final RequestContextExtension requestContext = RequestContextExtension.tenant(tenantId);

    @Test
    @DisplayName("usuarios de suporte sao listados somente no tenant atual")
    void shouldListUsersByCurrentTenantOnly() {
        when(userRepository.findAllByTenantIdOrderByNameAsc(tenantId)).thenReturn(List.of());

        new UserService(userRepository).listAll();

        verify(userRepository).findAllByTenantIdOrderByNameAsc(tenantId);
    }

    @Test
    @DisplayName("tickets de admin sao listados somente no tenant atual")
    void shouldListAdminTicketsByCurrentTenantOnly() {
        when(ticketRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId)).thenReturn(List.of());

        new TicketService(ticketRepository, userService, agentService, categoryService, accountRepository, mapper)
                .listAccessible(Optional.empty(), "admin@tenant.local", true);

        verify(ticketRepository).findAllByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Test
    @DisplayName("tickets de cliente sao listados por tenant e email")
    void shouldListCustomerTicketsByTenantAndEmail() {
        when(ticketRepository.findAllByTenantIdAndUserEmailIgnoreCaseOrderByCreatedAtDesc(tenantId, "cliente@tenant.local"))
                .thenReturn(List.of());

        new TicketService(ticketRepository, userService, agentService, categoryService, accountRepository, mapper)
                .listAccessible(Optional.empty(), "cliente@tenant.local", false);

        verify(ticketRepository).findAllByTenantIdAndUserEmailIgnoreCaseOrderByCreatedAtDesc(tenantId, "cliente@tenant.local");
    }
}
