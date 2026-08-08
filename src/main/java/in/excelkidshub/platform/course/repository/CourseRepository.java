package in.excelkidshub.platform.course.repository;

import in.excelkidshub.platform.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Course entity.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Find active courses.
     */
    List<Course> findByActiveTrue();

    /**
     * Find free courses.
     */
    List<Course> findByIsFreeTrueAndActiveTrue();

    /**
     * Find courses by level name.
     */
    List<Course> findByLevelNameAndActiveTrue(String levelName);

    /**
     * Find courses by age group.
     */
    List<Course> findByAgeGroupAndActiveTrue(String ageGroup);

    /**
     * Search courses by title (case-insensitive).
     */
    @Query("SELECT c FROM Course c WHERE c.active = true AND LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchByTitle(@Param("keyword") String keyword);

    /**
     * Find course by title.
     */
    Optional<Course> findByTitle(String title);

    /**
     * Find course by slug (URL path identifier, e.g. "phonics/level-1").
     */
    Optional<Course> findBySlugAndActiveTrue(String slug);

    /**
     * Find courses accessible via a list of IDs.
     */
    List<Course> findByIdInAndActiveTrue(List<Long> ids);
}
