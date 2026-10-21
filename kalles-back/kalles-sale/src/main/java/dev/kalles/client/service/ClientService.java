package dev.kalles.client.service;

import dev.kalles.client.dto.ClientRequest;
import dev.kalles.client.dto.ClientResponse;
import dev.kalles.client.entity.Client;
import dev.kalles.client.exception.ClientDocumentAlreadyExistsException;
import dev.kalles.client.exception.ClientPersonTypeImmutableException;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.shared.exception.NotFoundException;
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
        return clientRepository.findAllByCompanyIdOrderByNameAsc(CompanyContextHolder.requireCompanyId())
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> listPage(int page, int size) {
        return clientRepository.findAllByCompanyIdOrderByNameAsc(
                        CompanyContextHolder.requireCompanyId(),
                        PageRequest.of(page, size)
                )
                .map(ClientResponse::from);
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        assertDocumentAvailable(request.document(), companyId, null);

        Client client = new Client();
        client.setCompanyId(companyId);
        client.setPersonType(request.personType());
        apply(client, request);
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(UUID id) {
        return clientRepository.findByIdAndCompanyId(id, CompanyContextHolder.requireCompanyId())
                .map(ClientResponse::from)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
    }

    @Transactional
    public ClientResponse update(UUID id, ClientRequest request) {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        Client client = clientRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));

        if (client.getPersonType() != request.personType()) {
            throw new ClientPersonTypeImmutableException();
        }
        assertDocumentAvailable(request.document(), companyId, id);

        apply(client, request);
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public void delete(UUID id) {
        Client client = clientRepository.findByIdAndCompanyId(id, CompanyContextHolder.requireCompanyId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
        clientRepository.delete(client);
    }

    private void assertDocumentAvailable(String document, UUID companyId, UUID currentId) {
        clientRepository.findByDocumentAndCompanyId(document, companyId)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ClientDocumentAlreadyExistsException();
                });
    }

    private void apply(Client client, ClientRequest request) {
        client.setDocument(request.document());
        client.setName(request.name());
        client.setTradeName(request.tradeName());
        client.setStateRegistration(request.stateRegistration());
        client.setGender(request.gender());
        client.setCodeCountry(request.codeCountry());
        client.setCellphone(request.cellphone());
        client.setBirthDate(request.birthDate());
        client.setRg(request.rg());
        client.setNameFather(request.nameFather());
        client.setNameMother(request.nameMother());
        client.setObservations(request.observations());
    }
}
