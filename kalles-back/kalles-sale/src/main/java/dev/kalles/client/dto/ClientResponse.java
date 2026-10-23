package dev.kalles.client.dto;

import dev.kalles.client.entity.Client;
import dev.kalles.client.entity.PersonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

public record ClientResponse(

    UUID id,

    @Schema(description = "Tipo de pessoa: FISICA ou JURIDICA")
    PersonType personType,

    @Schema(description = "CPF ou CNPJ, sem pontuação")
    String document,

    @Schema(description = "Nome completo (pessoa física) ou razão social (pessoa jurídica)")
    String name,

    @Schema(description = "Nome fantasia")
    String tradeName,

    @Schema(description = "Inscrição estadual")
    String stateRegistration,

    @Schema(description = "Data de nascimento")
    LocalDate birthDate,

    @Schema(description = "Gênero: M, F ou O")
    Character gender,

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
            client.getPersonType(),
            client.getDocument(),
            client.getName(),
            client.getTradeName(),
            client.getStateRegistration(),
            client.getBirthDate(),
            client.getGender(),
            client.getCodeCountry(),
            client.getCellphone(),
            client.getRg(),
            client.getNameFather(),
            client.getNameMother(),
            client.getObservations()
        );
    }
}
