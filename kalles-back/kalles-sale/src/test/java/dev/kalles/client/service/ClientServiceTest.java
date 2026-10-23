package dev.kalles.client.service;

import dev.kalles.client.dto.ClientRequest;
import dev.kalles.client.dto.ClientResponse;
import dev.kalles.client.entity.Client;
import dev.kalles.client.entity.PersonType;
import dev.kalles.client.exception.ClientDocumentAlreadyExistsException;
import dev.kalles.client.exception.ClientPersonTypeImmutableException;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.security.exception.CompanyContextRequiredException;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.testsupport.RequestContextExtension;
import dev.kalles.security.context.RequestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService - Serviço de Clientes")
class ClientServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.company(COMPANY_ID);

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private ClientRequest buildRequest(String cpf) {
        return naturalPersonRequest("João da Silva", cpf);
    }

    private ClientRequest naturalPersonRequest(String name, String cpf) {
        return new ClientRequest(PersonType.FISICA, cpf, name, null, null, LocalDate.of(1990, 5, 20), 'M',
                "+55", "11999999999", "1234567", "Pai Teste", "Mãe Teste", null);
    }

    private ClientRequest legalEntityRequest(String cnpj) {
        return new ClientRequest(PersonType.JURIDICA, cnpj, "Aurora Distribuidora Ltda", "Aurora Alimentos", "isento",
                null, null, "+55", "11999999999", null, null, null, null);
    }

    private Client buildClient(UUID id, String cpf) {
        Client c = new Client();
        c.setId(id);
        c.setCompanyId(COMPANY_ID);
        c.setName("João da Silva");
        c.setBirthDate(LocalDate.of(1990, 5, 20));
        c.setGender('M');
        c.setDocument(cpf);
        c.setCodeCountry("+55");
        c.setCellphone("11999999999");
        return c;
    }

    @Test
    @DisplayName("Deve criar cliente com sucesso")
    void shouldCreateClientSuccessfully() {
        UUID id = UUID.randomUUID();
        String cpf = "529.982.247-25";
        // O ClientRequest normaliza o CPF (só dígitos) antes da consulta
        String normalizedCpf = "52998224725";
        Client saved = buildClient(id, cpf);

        when(clientRepository.findByDocumentAndCompanyId(normalizedCpf, COMPANY_ID)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(saved);

        ClientResponse response = clientService.create(buildRequest(cpf));

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("João da Silva", response.name());
        verify(clientRepository).findByDocumentAndCompanyId(normalizedCpf, COMPANY_ID);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com CPF duplicado")
    void shouldThrowWhenCreatingClientWithDuplicateCpf() {
        String cpf = "529.982.247-25";
        Client existing = buildClient(UUID.randomUUID(), cpf);

        when(clientRepository.findByDocumentAndCompanyId("52998224725", COMPANY_ID)).thenReturn(Optional.of(existing));

        assertThrows(ClientDocumentAlreadyExistsException.class, () -> clientService.create(buildRequest(cpf)));
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar cliente pessoa jurídica com tipo, nome fantasia e inscrição estadual")
    void shouldCreateLegalEntityClient() {
        when(clientRepository.findByDocumentAndCompanyId("12ABC34501DE35", COMPANY_ID)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientResponse response = clientService.create(legalEntityRequest("12.abc.345/01de-35"));

        assertEquals(PersonType.JURIDICA, response.personType());
        assertEquals("12ABC34501DE35", response.document());
        assertEquals("Aurora Alimentos", response.tradeName());
        assertEquals("ISENTO", response.stateRegistration());
    }

    @Test
    @DisplayName("Deve encontrar cliente pelo ID")
    void shouldFindClientById() {
        UUID id = UUID.randomUUID();
        Client client = buildClient(id, "529.982.247-25");
        when(clientRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(client));

        ClientResponse response = clientService.findById(id);

        assertEquals(id, response.id());
        assertEquals("João da Silva", response.name());
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não encontrado pelo ID")
    void shouldThrowNotFoundWhenClientNotFoundById() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> clientService.findById(id));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    @DisplayName("Deve listar todos os clientes ordenados por nome")
    void shouldListAllClientsOrderedByName() {
        List<Client> clients = List.of(
                buildClient(UUID.randomUUID(), "111"),
                buildClient(UUID.randomUUID(), "222")
        );
        when(clientRepository.findAllByCompanyIdOrderByNameAsc(COMPANY_ID)).thenReturn(clients);

        List<ClientResponse> result = clientService.listAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void shouldUpdateClientSuccessfully() {
        UUID id = UUID.randomUUID();
        String cpf = "529.982.247-25";
        Client existing = buildClient(id, cpf);
        Client updated = buildClient(id, cpf);
        updated.setName("Nome Atualizado");

        when(clientRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(existing));
        when(clientRepository.findByDocumentAndCompanyId("52998224725", COMPANY_ID)).thenReturn(Optional.of(existing));
        when(clientRepository.save(any(Client.class))).thenReturn(updated);

        ClientRequest request = naturalPersonRequest("Nome Atualizado", cpf);
        ClientResponse response = clientService.update(id, request);

        assertEquals("Nome Atualizado", response.name());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar com CPF pertencente a outro cliente")
    void shouldThrowWhenUpdatingClientWithCpfBelongingToAnotherClient() {
        UUID id = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        String cpf = "529.982.247-25";

        Client existing = buildClient(id, "outro-cpf");
        Client other = buildClient(otherId, cpf);

        when(clientRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(existing));
        when(clientRepository.findByDocumentAndCompanyId("52998224725", COMPANY_ID)).thenReturn(Optional.of(other));

        ClientRequest request = naturalPersonRequest("Xavier", cpf);

        assertThrows(ClientDocumentAlreadyExistsException.class, () -> clientService.update(id, request));
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve recusar a troca do tipo de pessoa na atualização")
    void shouldRejectPersonTypeChangeOnUpdate() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(buildClient(id, "52998224725")));

        assertThrows(ClientPersonTypeImmutableException.class,
                () -> clientService.update(id, legalEntityRequest("11222333000181")));
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cliente inexistente")
    void shouldThrowNotFoundWhenUpdatingNonExistentClient() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> clientService.update(id, buildRequest("529.982.247-25")));
    }

    @Test
    @DisplayName("Deve excluir cliente com sucesso")
    void shouldDeleteClientSuccessfully() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.of(buildClient(id, "529.982.247-25")));

        clientService.delete(id);

        verify(clientRepository).delete(any(Client.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir cliente inexistente")
    void shouldThrowNotFoundWhenDeletingNonExistentClient() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findByIdAndCompanyId(id, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.delete(id));
        verify(clientRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando não há company no contexto")
    void shouldThrowWhenCompanyContextIsMissing() {
        RequestContext.runWithin(RequestContext.empty(), () ->
                assertThrows(CompanyContextRequiredException.class, () -> clientService.listAll()));
    }
}
