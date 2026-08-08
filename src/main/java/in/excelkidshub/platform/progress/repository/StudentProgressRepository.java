package in.excelkidshub.platform.progress.repository;

import in.excelkidshub.platform.progress.entity.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentProgress entity.
 * Note: StudentProgress uses @ManyToOne relationships, so derived queries
 * use the nested path (e.g. user.id), not a direct userId column field.
 */
@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {

    /**
     * Find all progress records for a user.
     */
    List<StudentProgress> findByUserId(Long userId);

    /**
     * Find progress by user ID and course ID.
     */
    Optional<StudentProgress> findByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * Find active progress records for a user, newest first.
     */
    @Query("SELECT sp FROM StudentProgress sp WHERE sp.user.id = :userId AND sp.active = true ORDER BY sp.lastAccessed DESC")
    List<StudentProgress> findRecentlyAccessedByUserId(@Param("userId") Long userId);

    /**
     * Find progress by user ID and status.
     */
    List<StudentProgress> findByUserIdAndStatus(Long userId, String status);

    /**
     * Find progress by course ID.
     */
    List<StudentProgress> findByCourseId(Long courseId);

    /**
     * Check if progress exists for user and course.
     */
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
