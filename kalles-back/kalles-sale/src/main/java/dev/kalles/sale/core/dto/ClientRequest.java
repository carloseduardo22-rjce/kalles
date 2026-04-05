package dev.kalles.sale.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import org.hibernate.validator.constraints.br.CPF;

public record ClientRequest(

    @Schema(description = "Nome completo do cliente", example = "João da Silva")
    @NotBlank @Size(min = 3, max = 100)
    String name,

    @Schema(description = "Data de nascimento", example = "1990-05-20")
    LocalDate birthDate,

    @Schema(description = "Gênero: M (Masculino), F (Feminino), O (Outro)", example = "M")
    Character gender,

    @Schema(description = "CPF do cliente (somente números)", example = "12345678901")
    @Size(max = 14)
    @CPF
    @NotBlank
    String cpf,

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

    @Schema(description = "RG do cliente", example = "1234567")
    @Size(max = 20)
    String rg,

    @Schema(description = "Nome do pai")
    @Size(max = 100)
    String nameFather,

    @Schema(description = "Nome da mãe")
    @Size(max = 100)
    String nameMother,

    @Schema(description = "Observações gerais sobre o cliente")
    String observations
) {}
