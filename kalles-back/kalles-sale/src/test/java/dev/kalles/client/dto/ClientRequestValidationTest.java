package dev.kalles.client.dto;

import dev.kalles.client.entity.PersonType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ClientRequest - Validação por tipo de pessoa")
class ClientRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("Aceita pessoa física com CPF válido")
    void shouldAcceptNaturalPersonWithValidCpf() {
        ClientRequest request = naturalPerson("529.982.247-25", null);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.document()).isEqualTo("52998224725");
    }

    @Test
    @DisplayName("Recusa nome fantasia em pessoa física")
    void shouldRejectTradeNameForNaturalPerson() {
        assertThat(invalidFields(naturalPerson("52998224725", "Loja da Maria"))).containsExactly("tradeName");
    }

    @Test
    @DisplayName("Aceita CNPJ alfanumérico em minúsculas e com pontuação, normalizado")
    void shouldAcceptLowercasePunctuatedAlphanumericCnpj() {
        ClientRequest request = legalEntity("12.abc.345/01de-35", "isento", null);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.document()).isEqualTo("12ABC34501DE35");
        assertThat(request.stateRegistration()).isEqualTo("ISENTO");
    }

    @Test
    @DisplayName("Recusa CNPJ com dígito verificador inválido")
    void shouldRejectCnpjWithInvalidCheckDigit() {
        assertThat(invalidFields(legalEntity("12ABC34501DE36", null, null))).containsExactly("document");
    }

    @Test
    @DisplayName("Recusa RG em pessoa jurídica")
    void shouldRejectRgForLegalEntity() {
        assertThat(invalidFields(legalEntity("11222333000181", null, "1234567"))).containsExactly("rg");
    }

    @Test
    @DisplayName("Recusa inscrição estadual com letras que não sejam ISENTO")
    void shouldRejectStateRegistrationWithLetters() {
        assertThat(invalidFields(legalEntity("11222333000181", "ABC123", null))).containsExactly("stateRegistration");
    }

    @Test
    @DisplayName("Recusa cliente sem tipo de pessoa")
    void shouldRejectMissingPersonType() {
        ClientRequest request = new ClientRequest(null, "11222333000181", "Aurora Distribuidora Ltda", null, null,
                null, null, null, null, null, null, null, null);

        assertThat(invalidFields(request)).containsExactly("personType");
    }

    private Set<String> invalidFields(ClientRequest request) {
        Set<ConstraintViolation<ClientRequest>> violations = validator.validate(request);
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private ClientRequest naturalPerson(String cpf, String tradeName) {
        return new ClientRequest(PersonType.FISICA, cpf, "Maria Souza", tradeName, null,
                LocalDate.of(1990, 5, 20), 'F', "+55", "11999999999", null, null, null, null);
    }

    private ClientRequest legalEntity(String cnpj, String stateRegistration, String rg) {
        return new ClientRequest(PersonType.JURIDICA, cnpj, "Aurora Distribuidora Ltda", "Aurora Alimentos",
                stateRegistration, null, null, "+55", "11999999999", rg, null, null, null);
    }
}
