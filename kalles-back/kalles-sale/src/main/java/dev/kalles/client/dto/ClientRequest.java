package dev.kalles.client.dto;

import dev.kalles.client.dto.ClientRequestGroups.LegalEntity;
import dev.kalles.client.dto.ClientRequestGroups.NaturalPerson;
import dev.kalles.client.entity.PersonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Locale;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.group.GroupSequenceProvider;

@GroupSequenceProvider(ClientRequestGroups.class)
public record ClientRequest(

    @Schema(description = "Tipo de pessoa: FISICA ou JURIDICA", example = "FISICA")
    @NotNull
    PersonType personType,

    @Schema(description = "CPF (pessoa física) ou CNPJ numérico ou alfanumérico (pessoa jurídica)", example = "52998224725")
    @NotBlank
    @CPF(groups = NaturalPerson.class, message = "CPF inválido")
    @CNPJ(format = CNPJ.Format.ALPHANUMERIC, groups = LegalEntity.class, message = "CNPJ inválido")
    String document,

    @Schema(description = "Nome completo (pessoa física) ou razão social (pessoa jurídica)", example = "João da Silva")
    @NotBlank @Size(min = 3, max = 100)
    String name,

    @Schema(description = "Nome fantasia, somente pessoa jurídica", example = "Aurora Alimentos")
    @Size(max = 100)
    @Null(groups = NaturalPerson.class, message = "não se aplica a pessoa física")
    String tradeName,

    @Schema(description = "Inscrição estadual, somente pessoa jurídica: dígitos ou ISENTO", example = "ISENTO")
    @Pattern(regexp = "^(ISENTO|\\d{2,14})$", groups = LegalEntity.class, message = "Inscrição estadual deve ter só dígitos ou ser ISENTO")
    @Null(groups = NaturalPerson.class, message = "não se aplica a pessoa física")
    String stateRegistration,

    @Schema(description = "Data de nascimento, somente pessoa física", example = "1990-05-20")
    @PastOrPresent(message = "Data de nascimento não pode estar no futuro")
    @Null(groups = LegalEntity.class, message = "não se aplica a pessoa jurídica")
    LocalDate birthDate,

    @Schema(description = "Gênero, somente pessoa física: M (Masculino), F (Feminino), O (Outro)", example = "M")
    @Null(groups = LegalEntity.class, message = "não se aplica a pessoa jurídica")
    Character gender,

    @Schema(description = "Código do país para o telefone (DDI)", example = "+55")
    @Size(max = 5)
    @Pattern(regexp = "^(\\+55|55)?$", message = "Código do país deve ser +55")
    String codeCountry,

    @Schema(description = "Número de celular", example = "11999999999")
    @Size(max = 20)
    @Pattern(
        regexp = "^(|\\(?[1-9]{2}\\)?\\s?9\\d{4}-?\\d{4})$",
        message = "Celular deve ser um número brasileiro válido com 11 dígitos"
    )
    String cellphone,

    @Schema(description = "RG, somente pessoa física", example = "1234567")
    @Size(max = 20)
    @Null(groups = LegalEntity.class, message = "não se aplica a pessoa jurídica")
    String rg,

    @Schema(description = "Nome do pai, somente pessoa física")
    @Size(max = 100)
    @Null(groups = LegalEntity.class, message = "não se aplica a pessoa jurídica")
    String nameFather,

    @Schema(description = "Nome da mãe, somente pessoa física")
    @Size(max = 100)
    @Null(groups = LegalEntity.class, message = "não se aplica a pessoa jurídica")
    String nameMother,

    @Schema(description = "Observações gerais sobre o cliente")
    String observations
) {
    public ClientRequest {
        document = alphanumericUpperCase(document);
        name = normalizeRequired(name);
        tradeName = normalizeOptional(tradeName);
        stateRegistration = alphanumericUpperCase(stateRegistration);
        codeCountry = normalizeOptional(codeCountry);
        cellphone = digitsOnly(cellphone);
        rg = normalizeOptional(rg);
        nameFather = normalizeOptional(nameFather);
        nameMother = normalizeOptional(nameMother);
        observations = normalizeOptional(observations);
    }

    private static String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String digitsOnly(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\D", "");
        return normalized.isEmpty() ? null : normalized;
    }

    private static String alphanumericUpperCase(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("[^0-9A-Za-z]", "").toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
