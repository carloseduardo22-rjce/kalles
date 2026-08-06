package dev.kalles.sale.goal.steps;

import dev.kalles.sale.core.repository.SaleRepository;
import dev.kalles.sale.goal.entity.Goal;
import dev.kalles.sale.goal.enums.GoalStatus;
import dev.kalles.sale.goal.enums.Periodicity;
import dev.kalles.sale.goal.exception.GoalDomainException;
import dev.kalles.sale.goal.service.GoalAssessmentResult;
import dev.kalles.sale.goal.service.GoalAssessmentService;
import dev.kalles.sale.goal.service.OverlapValidator;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class BillingGoalSteps {

    private static final UUID COMPANY_ID = UUID.fromString("e28a38a0-2f22-4a00-9e6b-67e9f3b5c65f");

    private final List<Goal> registeredGoals = new ArrayList<>();
    private final GoalAssessmentService assessmentService =
            new GoalAssessmentService(mock(SaleRepository.class));

    private Goal createdGoal;
    private Goal contextGoal;
    private Exception capturedException;
    private GoalAssessmentResult assessmentResult;
    private final List<BigDecimal> periodSales = new ArrayList<>();

    @Given("que nao existe nenhuma Meta com Periodicidade {string} ativa entre {string} e {string}")
    public void noActiveGoalExists(String periodicity, String startDate, String endDate) {
        registeredGoals.clear();
    }

    @Given("que existe uma Meta com Periodicidade {string} e status {string} entre {string} e {string}")
    public void activeGoalExists(String periodicity, String status, String startDate, String endDate) {
        Goal goal = Goal.create(
            COMPANY_ID,
                new BigDecimal("90000.00"),
                Periodicity.valueOf(periodicity),
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        if (GoalStatus.valueOf(status) == GoalStatus.ACTIVE) {
            goal.activate();
        }
        registeredGoals.add(goal);
        contextGoal = goal;
    }

    @Given("que existe uma Meta com Periodicidade {string} e status {string} com valor alvo de {string} entre {string} e {string}")
    public void activeGoalExistsWithTargetValue(String periodicity, String status, String targetValue, String startDate, String endDate) {
        Goal goal = Goal.create(
            COMPANY_ID,
                new BigDecimal(targetValue),
                Periodicity.valueOf(periodicity),
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        if (GoalStatus.valueOf(status) == GoalStatus.ACTIVE) {
            goal.activate();
        }
        registeredGoals.add(goal);
        contextGoal = goal;
    }

    @Given("que as seguintes vendas foram concluidas no periodo:")
    public void salesCompletedInPeriod(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            periodSales.add(new BigDecimal(row.get("valorTotal")));
        }
    }

    @When("o gestor cria uma Meta com os seguintes dados:")
    public void managerCreatesGoal(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();
        try {
            Goal goal = Goal.create(
                    COMPANY_ID,
                    new BigDecimal(data.get("valorAlvo")),
                    Periodicity.valueOf(data.get("periodicidade")),
                    LocalDate.parse(data.get("dataInicio")),
                    LocalDate.parse(data.get("dataFim"))
            );
            OverlapValidator.validate(registeredGoals, goal);
            registeredGoals.add(goal);
            createdGoal = goal;
        } catch (GoalDomainException e) {
            capturedException = e;
        }
    }

    @When("o gestor tenta criar uma nova Meta com os seguintes dados:")
    public void managerTriesToCreateOverlappingGoal(DataTable dataTable) {
        managerCreatesGoal(dataTable);
    }

    @When("o gestor solicita a apuracao da Meta")
    public void managerRequestsAssessment() {
        BigDecimal totalSold = periodSales.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assessmentResult = assessmentService.assess(contextGoal, totalSold);
    }

    @Then("a criacao da Meta deve ser bem-sucedida")
    public void goalCreationShouldSucceed() {
        assertThat(capturedException).isNull();
        assertThat(createdGoal).isNotNull();
    }

    @And("o status da Meta deve ser {string}")
    public void goalStatusShouldBe(String status) {
        assertThat(createdGoal.getStatus()).isEqualTo(GoalStatus.valueOf(status));
    }

    @And("o valor alvo da Meta deve ser {string}")
    public void goalTargetValueShouldBe(String expectedValue) {
        assertThat(createdGoal.getTargetValue()).isEqualByComparingTo(new BigDecimal(expectedValue));
    }

    @Then("a criacao da Meta deve ser recusada")
    public void goalCreationShouldBeRejected() {
        assertThat(capturedException).isNotNull();
    }

    @And("a mensagem de erro da Meta deve ser {string}")
    public void goalErrorMessageShouldBe(String expectedMessage) {
        assertThat(capturedException).hasMessage(expectedMessage);
    }

    @Then("o valor realizado deve ser {string}")
    public void achievedValueShouldBe(String expectedValue) {
        assertThat(assessmentResult.achievedValue()).isEqualByComparingTo(new BigDecimal(expectedValue));
    }

    @And("a lacuna deve ser {string}")
    public void gapShouldBe(String expectedGap) {
        assertThat(assessmentResult.gap()).isEqualByComparingTo(new BigDecimal(expectedGap));
    }
}