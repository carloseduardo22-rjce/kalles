package dev.kalles.sale.core.dto;

import dev.kalles.sale.core.entity.Client;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

public record ClientResponse(

    UUID id,

    @Schema(description = "Nome completo do cliente")
    String name,

    @Schema(description = "Data de nascimento")
    LocalDate birthDate,

    @Schema(description = "Gênero: M, F ou O")
    Character gender,

    @Schema(description = "CPF do cliente")
    String cpf,

    @Schema(description = "Código do país (DDI)")
    String codeCountry,

    @Schema(description = "Número de celular")
    String cellphone,

    @Schema(description = "RG do cliente")
    String rg,

    @Schema(description = "Nome do pai")
    String nameFather,

    @Schema(description = "Nome da mãe")
    String nameMother,

    @Schema(description = "Observações gerais")
    String observations
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
            client.getId(),
            client.getName(),
            client.getBirthDate(),
            client.getGender(),
            client.getCpf(),
            client.getCodeCountry(),
            client.getCellphone(),
            client.getRg(),
            client.getNameFather(),
            client.getNameMother(),
            client.getObservations()
        );
    }
}
