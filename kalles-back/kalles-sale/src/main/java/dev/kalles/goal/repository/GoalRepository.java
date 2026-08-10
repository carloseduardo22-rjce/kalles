package dev.kalles.goal.repository;

import dev.kalles.goal.entity.Goal;
import dev.kalles.goal.enums.GoalStatus;
import dev.kalles.goal.enums.Periodicity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

    List<Goal> findByCompanyIdAndPeriodicityAndStatus(UUID companyId, Periodicity periodicity, GoalStatus status);

    List<Goal> findAllByCompanyIdOrderByStartDateDesc(UUID companyId);

    Optional<Goal> findByIdAndCompanyId(UUID id, UUID companyId);
}
