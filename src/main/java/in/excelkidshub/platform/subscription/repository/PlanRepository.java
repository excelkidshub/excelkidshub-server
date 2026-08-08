package in.excelkidshub.platform.subscription.repository;

import in.excelkidshub.platform.subscription.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Plan entity.
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    /**
     * Find active plans.
     */
    List<Plan> findByActiveTrue();

    /**
     * Find popular plans.
     */
    List<Plan> findByIsPopularTrueAndActiveTrue();

    /**
     * Find plan by name.
     */
    Plan findByName(String name);
}
