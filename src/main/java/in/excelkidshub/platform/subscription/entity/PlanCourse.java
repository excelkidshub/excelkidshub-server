package in.excelkidshub.platform.subscription.entity;

import in.excelkidshub.platform.common.entity.BaseEntity;
import in.excelkidshub.platform.course.entity.Course;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * PlanCourse entity representing many-to-many relationship between Plan and Course.
 * Junction table with additional audit fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "plan_courses", indexes = {
    @Index(name = "idx_plan_courses_plan_id", columnList = "plan_id"),
    @Index(name = "idx_plan_courses_course_id", columnList = "course_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_plan_courses", columnNames = {"plan_id", "course_id"})
})
public class PlanCourse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_plan_courses_plan"))
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, foreignKey = @ForeignKey(name = "fk_plan_courses_course"))
    private Course course;
}
