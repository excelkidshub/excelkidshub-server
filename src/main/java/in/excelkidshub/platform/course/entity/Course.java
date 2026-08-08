package in.excelkidshub.platform.course.entity;

import in.excelkidshub.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Course entity representing educational courses.
 * Supports unlimited courses with generic level names (not hardcoded).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_courses_title", columnList = "title"),
    @Index(name = "idx_courses_level_name", columnList = "level_name"),
    @Index(name = "idx_courses_age_group", columnList = "age_group"),
    @Index(name = "idx_courses_active", columnList = "active"),
    @Index(name = "idx_courses_is_free", columnList = "is_free")
})
public class Course extends BaseEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "level_name", nullable = false, length = 100)
    private String levelName;

    @Column(name = "age_group", length = 50)
    private String ageGroup;

    @Column(name = "difficulty_level", length = 50)
    private String difficultyLevel;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "total_lessons")
    private Integer totalLessons;

    @Column(name = "is_free", nullable = false)
    @Builder.Default
    private Boolean isFree = false;

    @Column(name = "slug", unique = true, length = 255)
    private String slug;
}
