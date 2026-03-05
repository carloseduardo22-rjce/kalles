package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.ClientRequest;
import dev.kalles.sale.core.dto.ClientResponse;
import dev.kalles.sale.core.entity.Client;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public ClientResponse create(ClientRequest request) {
        if (request.cpf() != null && !request.cpf().isBlank()) {
            clientRepository.findByCpf(request.cpf()).ifPresent(existing -> {
                throw new IllegalArgumentException("Já existe um cliente cadastrado com o CPF informado.");
            });
        }
        Client client = new Client();
        client.setName(request.name());
        client.setBirthDate(request.birthDate());
        client.setGender(request.gender());
        client.setCpf(request.cpf());
        client.setCodeCountry(request.codeCountry());
        client.setCellphone(request.cellphone());
        client.setRg(request.rg());
        client.setNameFather(request.nameFather());
        client.setNameMother(request.nameMother());
        client.setObservations(request.observations());
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(UUID id) {
        return clientRepository.findById(id)
                .map(ClientResponse::from)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> listAll() {
        return clientRepository.findAllByOrderByNameAsc()
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @Transactional
    public ClientResponse update(UUID id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado: " + id));

        if (request.cpf() != null && !request.cpf().isBlank()) {
            clientRepository.findByCpf(request.cpf()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Já existe um cliente cadastrado com o CPF informado.");
                }
            });
        }

        client.setName(request.name());
        client.setBirthDate(request.birthDate());
        client.setGender(request.gender());
        client.setCpf(request.cpf());
        client.setCodeCountry(request.codeCountry());
        client.setCellphone(request.cellphone());
        client.setRg(request.rg());
        client.setNameFather(request.nameFather());
        client.setNameMother(request.nameMother());
        client.setObservations(request.observations());
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public void delete(UUID id) {
        if (!clientRepository.existsById(id)) {
            throw new NotFoundException("Cliente não encontrado: " + id);
        }
        clientRepository.deleteById(id);
    }

}
