package dev.kalles.client.service;

import dev.kalles.client.dto.ClientRequest;
import dev.kalles.client.dto.ClientResponse;
import dev.kalles.client.entity.Client;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.exception.CompanyContextRequiredException;
import dev.kalles.shared.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        CompanyContextHolder.setCompanyId(COMPANY_ID);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
    }

    private ClientRequest buildRequest(String cpf) {
        return new ClientRequest("João da Silva", LocalDate.of(1990, 5, 20), 'M',
                cpf, "+55", "11999999999", "1234567", "Pai Teste", "Mãe Teste", null);
    }

    private Client buildClient(UUID id, String cpf) {
        Client c = new Client();
        c.setId(id);
        c.setCompanyId(COMPANY_ID);
        c.setName("João da Silva");
        c.setBirthDate(LocalDate.of(1990, 5, 20));
        c.setGender('M');
        c.setCpf(cpf);
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

        when(clientRepository.findByCpfAndCompanyId(normalizedCpf, COMPANY_ID)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(saved);

        ClientResponse response = clientService.create(buildRequest(cpf));

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("João da Silva", response.name());
        verify(clientRepository).findByCpfAndCompanyId(normalizedCpf, COMPANY_ID);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com CPF duplicado")
    void shouldThrowWhenCreatingClientWithDuplicateCpf() {
        String cpf = "529.982.247-25";
        Client existing = buildClient(UUID.randomUUID(), cpf);

        when(clientRepository.findByCpfAndCompanyId("52998224725", COMPANY_ID)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> clientService.create(buildRequest(cpf)));
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar cliente com CPF nulo sem consultar o repositório")
    void shouldCreateClientWithNullCpfWithoutCheckingRepository() {
        Client saved = buildClient(UUID.randomUUID(), null);
        when(clientRepository.save(any(Client.class))).thenReturn(saved);

        ClientRequest request = new ClientRequest("João da Silva", null, null,
                null, null, null, null, null, null, null);

        assertDoesNotThrow(() -> clientService.create(request));
        verify(clientRepository, never()).findByCpfAndCompanyId(any(), any());
        verify(clientRepository).save(any());
    }

    @Test
    @DisplayName("Deve criar cliente com CPF em branco sem consultar o repositório")
    void shouldCreateClientWithBlankCpfWithoutCheckingRepository() {
        Client saved = buildClient(UUID.randomUUID(), "");
        when(clientRepository.save(any(Client.class))).thenReturn(saved);

        ClientRequest request = new ClientRequest("João da Silva", null, null,
                "", null, null, null, null, null, null);

        assertDoesNotThrow(() -> clientService.create(request));
        verify(clientRepository, never()).findByCpfAndCompanyId(any(), any());
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
        when(clientRepository.findByCpfAndCompanyId("52998224725", COMPANY_ID)).thenReturn(Optional.of(existing));
        when(clientRepository.save(any(Client.class))).thenReturn(updated);

        ClientRequest request = new ClientRequest("Nome Atualizado", null, null,
                cpf, null, null, null, null, null, null);
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
        when(clientRepository.findByCpfAndCompanyId("52998224725", COMPANY_ID)).thenReturn(Optional.of(other));

        ClientRequest request = new ClientRequest("X", null, null,
                cpf, null, null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> clientService.update(id, request));
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
        CompanyContextHolder.clear();

        assertThrows(CompanyContextRequiredException.class, () -> clientService.listAll());
    }
}
