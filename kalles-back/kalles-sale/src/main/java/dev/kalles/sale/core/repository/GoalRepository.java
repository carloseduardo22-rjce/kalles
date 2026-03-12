package dev.kalles.sale.core.repository;

import dev.kalles.sale.core.entity.Goal;
import dev.kalles.sale.core.enums.goal.GoalStatus;
import dev.kalles.sale.core.enums.goal.Periodicity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

    List<Goal> findByPeriodicityAndStatus(Periodicity periodicity, GoalStatus status);
}
