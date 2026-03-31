package dev.kalles.sale.core.entity;

import dev.kalles.sale.core.enums.goal.GoalStatus;
import dev.kalles.sale.core.enums.goal.Periodicity;
import dev.kalles.sale.core.exception.GoalDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "goals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "target_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal targetValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Periodicity periodicity;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status;

    public static Goal create(BigDecimal targetValue, Periodicity periodicity,
                               LocalDate startDate, LocalDate endDate) {
        if (targetValue == null) {
            throw new GoalDomainException("Target value is required");
        }
        if (targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GoalDomainException("Target value must be positive");
        }
        if (endDate.isBefore(startDate)) {
            throw new GoalDomainException("End date must be after start date");
        }
        Goal goal = new Goal();
        goal.targetValue = targetValue;
        goal.periodicity = periodicity;
        goal.startDate = startDate;
        goal.endDate = endDate;
        goal.status = GoalStatus.DRAFT;
        return goal;
    }

    public void activate() {
        this.status = GoalStatus.ACTIVE;
    }

    public void close() {
        this.status = GoalStatus.CLOSED;
    }

    public void changeTargetValue(BigDecimal newValue) {
        if (this.status == GoalStatus.ACTIVE) {
            throw new GoalDomainException("Active goal cannot have its target value changed");
        }
        this.targetValue = newValue;
    }

    public void changePeriod(LocalDate newStartDate, LocalDate newEndDate) {
        if (this.status == GoalStatus.ACTIVE) {
            throw new GoalDomainException("Active goal cannot have its dates changed");
        }
        this.startDate = newStartDate;
        this.endDate = newEndDate;
    }

    public boolean overlaps(Goal other) {
        return !this.endDate.isBefore(other.startDate) && !other.endDate.isBefore(this.startDate);
    }
}
