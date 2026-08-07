package dev.kalles.client.service;

import dev.kalles.client.dto.ClientRequest;
import dev.kalles.client.dto.ClientResponse;
import dev.kalles.client.entity.Client;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.core.exception.NotFoundException;
import dev.kalles.security.context.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<ClientResponse> listAll() {
        return clientRepository.findAllByCompanyIdOrderByNameAsc(getCompanyId())
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> listPage(int page, int size) {
        return clientRepository.findAllByCompanyIdOrderByNameAsc(
                        getCompanyId(),
                        PageRequest.of(page, size)
                )
                .map(ClientResponse::from);
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        UUID companyId = getCompanyId();

        if (request.cpf() != null && !request.cpf().isBlank()) {
            clientRepository.findByCpfAndCompanyId(request.cpf(), companyId).ifPresent(existing -> {
                throw new IllegalArgumentException("Já existe um cliente com o CPF informado nesta filial.");
            });
        }

        Client client = new Client();
        client.setCompanyId(companyId);
        client.setName(request.name());
        client.setCpf(request.cpf());
        client.setGender(request.gender());
        client.setCodeCountry(request.codeCountry());
        client.setCellphone(request.cellphone());
        client.setBirthDate(request.birthDate());
        client.setRg(request.rg());
        client.setNameFather(request.nameFather());
        client.setNameMother(request.nameMother());
        client.setObservations(request.observations());
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(UUID id) {
        return clientRepository.findByIdAndCompanyId(id, getCompanyId())
                .map(ClientResponse::from)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
    }

    @Transactional
    public ClientResponse update(UUID id, ClientRequest request) {
        UUID companyId = getCompanyId();
        Client client = clientRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));

        if (request.cpf() != null && !request.cpf().isBlank()) {
            clientRepository.findByCpfAndCompanyId(request.cpf(), companyId).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Já existe um cliente com o CPF informado nesta filial.");
                }
            });
        }

        client.setName(request.name());
        client.setCpf(request.cpf());
        client.setGender(request.gender());
        client.setCodeCountry(request.codeCountry());
        client.setCellphone(request.cellphone());
        client.setBirthDate(request.birthDate());
        client.setRg(request.rg());
        client.setNameFather(request.nameFather());
        client.setNameMother(request.nameMother());
        client.setObservations(request.observations());
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public void delete(UUID id) {
        Client client = clientRepository.findByIdAndCompanyId(id, getCompanyId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
        clientRepository.delete(client);
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operação.");
        }
        return companyId;
    }
}
