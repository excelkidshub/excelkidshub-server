package in.excelkidshub.platform.progress.repository;

import in.excelkidshub.platform.progress.entity.ActivityProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ActivityProgress entity.
 * Tracks practice activities, games, and assessments.
 */
@Repository
public interface ActivityProgressRepository extends JpaRepository<ActivityProgress, Long> {

    /**
     * Find all activity progress records for a user.
     */
    List<ActivityProgress> findByUserId(Long userId);

    /**
     * Find activity progress by user ID and activity ID.
     */
    Optional<ActivityProgress> findByUserIdAndActivityId(Long userId, String activityId);

    /**
     * Find activity progress by user ID and activity type.
     */
    List<ActivityProgress> findByUserIdAndActivityType(Long userId, String activityType);

    /**
     * Find completed activities for a user.
     */
    List<ActivityProgress> findByUserIdAndCompletedTrue(Long userId);

    /**
     * Count completed activities by type for a user.
     */
    @Query("SELECT COUNT(ap) FROM ActivityProgress ap WHERE ap.user.id = :userId AND ap.activityType = :activityType AND ap.completed = true")
    long countByUserIdAndActivityTypeAndCompletedTrue(@Param("userId") Long userId, @Param("activityType") String activityType);

    /**
     * Find recent activities for a user.
     */
    @Query("SELECT ap FROM ActivityProgress ap WHERE ap.user.id = :userId ORDER BY ap.completedAt DESC")
    List<ActivityProgress> findRecentByUserId(@Param("userId") Long userId);

    /**
     * Find activity progress by user, course, and activity ID.
     */
    Optional<ActivityProgress> findByUserIdAndCourseIdAndActivityId(Long userId, Long courseId, String activityId);
}
