package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.ClientRequest;
import dev.kalles.sale.core.dto.ClientResponse;
import dev.kalles.sale.core.entity.Client;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ClientService {

    private static final String BRAZIL_COUNTRY_CODE = "+55";
    private static final Pattern NON_DIGITS_PATTERN = Pattern.compile("\\D");
    private static final Pattern BRAZILIAN_MOBILE_PATTERN = Pattern.compile("^[1-9]{2}9\\d{8}$");

    private final ClientRepository clientRepository;

    @Transactional
    public ClientResponse create(ClientRequest request) {
        NormalizedClientData normalized = normalize(request);

        if (normalized.cpf() != null && !normalized.cpf().isBlank()) {
            clientRepository.findByCpf(normalized.cpf()).ifPresent(existing -> {
                throw new IllegalArgumentException("Já existe um cliente cadastrado com o CPF informado.");
            });
        }

        Client client = new Client();
        apply(client, normalized);
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
        NormalizedClientData normalized = normalize(request);

        if (normalized.cpf() != null && !normalized.cpf().isBlank()) {
            clientRepository.findByCpf(normalized.cpf()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Já existe um cliente cadastrado com o CPF informado.");
                }
            });
        }

        apply(client, normalized);
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public void delete(UUID id) {
        if (!clientRepository.existsById(id)) {
            throw new NotFoundException("Cliente não encontrado: " + id);
        }
        clientRepository.deleteById(id);
    }

    private void apply(Client client, NormalizedClientData normalized) {
        client.setName(normalized.name());
        client.setBirthDate(normalized.birthDate());
        client.setGender(normalized.gender());
        client.setCpf(normalized.cpf());
        client.setCodeCountry(normalized.codeCountry());
        client.setCellphone(normalized.cellphone());
        client.setRg(normalized.rg());
        client.setNameFather(normalized.nameFather());
        client.setNameMother(normalized.nameMother());
        client.setObservations(normalized.observations());
    }

    private NormalizedClientData normalize(ClientRequest request) {
        String cellphone = normalizeCellphone(request.cellphone());

        return new NormalizedClientData(
                trimToNull(request.name()),
                request.birthDate(),
                request.gender(),
                normalizeCpf(request.cpf()),
                normalizeCodeCountry(request.codeCountry(), cellphone),
                cellphone,
                trimToNull(request.rg()),
                trimToNull(request.nameFather()),
                trimToNull(request.nameMother()),
                trimToNull(request.observations())
        );
    }

    private String normalizeCpf(String cpf) {
        String digits = digitsOnly(cpf);
        return digits.isBlank() ? null : digits;
    }

    private String normalizeCellphone(String cellphone) {
        String digits = digitsOnly(cellphone);
        if (digits.isBlank()) {
            return null;
        }
        if (!BRAZILIAN_MOBILE_PATTERN.matcher(digits).matches()) {
            throw new IllegalArgumentException("Celular deve ser um número brasileiro válido com 11 dígitos.");
        }
        return digits;
    }

    private String normalizeCodeCountry(String codeCountry, String cellphone) {
        String digits = digitsOnly(codeCountry);

        if (cellphone == null) {
            return digits.isBlank() ? null : BRAZIL_COUNTRY_CODE;
        }
        if (!digits.isBlank() && !"55".equals(digits)) {
            throw new IllegalArgumentException("Código do país do celular deve ser +55.");
        }

        return BRAZIL_COUNTRY_CODE;
    }

    private String digitsOnly(String value) {
        return value == null ? "" : NON_DIGITS_PATTERN.matcher(value).replaceAll("");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private record NormalizedClientData(
            String name,
            LocalDate birthDate,
            Character gender,
            String cpf,
            String codeCountry,
            String cellphone,
            String rg,
            String nameFather,
            String nameMother,
            String observations
    ) {
    }
}
