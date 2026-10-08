package dev.kalles.support.service;

import dev.kalles.support.domain.Priority;
import dev.kalles.support.entity.AgentEntity;
import dev.kalles.support.entity.CategoryEntity;
import dev.kalles.support.entity.UserEntity;
import dev.kalles.support.exception.AgentEmployeeIdAlreadyExistsException;
import dev.kalles.support.exception.CategoryAlreadyExistsException;
import dev.kalles.support.exception.UserEmailAlreadyExistsException;
import dev.kalles.support.repository.AgentRepository;
import dev.kalles.support.repository.CategoryRepository;
import dev.kalles.support.repository.UserRepository;
import dev.kalles.testsupport.RequestContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Support - cadastros duplicados")
class SupportDuplicateRecordServiceTest {

    private final UUID tenantId = UUID.randomUUID();

    @RegisterExtension
    final RequestContextExtension requestContext = RequestContextExtension.tenant(tenantId);

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("recusa atendente com matricula ja cadastrada")
    void shouldRejectAgentWithExistingEmployeeId() {
        when(agentRepository.findByTenantIdAndEmployeeId(tenantId, "A-01")).thenReturn(Optional.of(new AgentEntity()));

        AgentEmployeeIdAlreadyExistsException error = assertThrows(AgentEmployeeIdAlreadyExistsException.class,
                () -> new AgentService(agentRepository).create("A-01", "Ana"));

        assertEquals("SUPPORT_AGENT_EMPLOYEE_ID_ALREADY_EXISTS", error.getCode());
        verify(agentRepository, never()).save(any());
    }

    @Test
    @DisplayName("recusa atualizar atendente com matricula de outro atendente")
    void shouldRejectAgentUpdateWithEmployeeIdOfAnotherAgent() {
        UUID agentId = UUID.randomUUID();
        AgentEntity current = new AgentEntity();
        current.setId(agentId);
        AgentEntity another = new AgentEntity();
        another.setId(UUID.randomUUID());
        when(agentRepository.findByIdAndTenantId(agentId, tenantId)).thenReturn(Optional.of(current));
        when(agentRepository.findByTenantIdAndEmployeeId(tenantId, "A-01")).thenReturn(Optional.of(another));

        assertThrows(AgentEmployeeIdAlreadyExistsException.class,
                () -> new AgentService(agentRepository).update(agentId, "A-01", "Ana"));
        verify(agentRepository, never()).save(any());
    }

    @Test
    @DisplayName("recusa categoria com nome e subcategoria ja cadastrados")
    void shouldRejectCategoryWithExistingNameAndSubcategory() {
        when(categoryRepository.findByTenantIdAndNameAndSubcategory(tenantId, "Fiscal", "NFC-e"))
                .thenReturn(Optional.of(new CategoryEntity()));

        CategoryAlreadyExistsException error = assertThrows(CategoryAlreadyExistsException.class,
                () -> new CategoryService(categoryRepository).create("Fiscal", "NFC-e", Priority.MEDIUM));

        assertEquals("SUPPORT_CATEGORY_ALREADY_EXISTS", error.getCode());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("recusa atualizar categoria com nome e subcategoria de outra categoria")
    void shouldRejectCategoryUpdateWithNameOfAnotherCategory() {
        UUID categoryId = UUID.randomUUID();
        CategoryEntity current = new CategoryEntity();
        current.setId(categoryId);
        CategoryEntity another = new CategoryEntity();
        another.setId(UUID.randomUUID());
        when(categoryRepository.findByIdAndTenantId(categoryId, tenantId)).thenReturn(Optional.of(current));
        when(categoryRepository.findByTenantIdAndNameAndSubcategory(tenantId, "Fiscal", "NFC-e"))
                .thenReturn(Optional.of(another));

        assertThrows(CategoryAlreadyExistsException.class,
                () -> new CategoryService(categoryRepository).update(categoryId, "Fiscal", "NFC-e", Priority.MEDIUM));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("recusa usuario com e-mail ja cadastrado")
    void shouldRejectUserWithExistingEmail() {
        when(userRepository.findByTenantIdAndEmailIgnoreCase(tenantId, "ana@tenant.local"))
                .thenReturn(Optional.of(new UserEntity()));

        UserEmailAlreadyExistsException error = assertThrows(UserEmailAlreadyExistsException.class,
                () -> new UserService(userRepository).create("ana@tenant.local", "Ana"));

        assertEquals("SUPPORT_USER_EMAIL_ALREADY_EXISTS", error.getCode());
        verify(userRepository, never()).save(any());
    }
}
