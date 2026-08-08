package in.excelkidshub.platform.subscription.repository;

import in.excelkidshub.platform.subscription.entity.PlanCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PlanCourse entity.
 */
@Repository
public interface PlanCourseRepository extends JpaRepository<PlanCourse, Long> {

    /**
     * Find plan-courses by plan ID.
     */
    List<PlanCourse> findByPlanId(Long planId);

    /**
     * Find plan-courses by course ID.
     */
    List<PlanCourse> findByCourseId(Long courseId);

    /**
     * Check if plan-course association exists.
     */
    boolean existsByPlanIdAndCourseId(Long planId, Long courseId);

    /**
     * Delete plan-course by plan ID and course ID.
     */
    void deleteByPlanIdAndCourseId(Long planId, Long courseId);
}
