package in.excelkidshub.platform.subscription.entity;

import in.excelkidshub.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Plan entity representing subscription plans.
 * Contains pricing, duration, and feature information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "plans", indexes = {
    @Index(name = "idx_plans_name", columnList = "name"),
    @Index(name = "idx_plans_active", columnList = "active"),
    @Index(name = "idx_plans_is_popular", columnList = "is_popular")
})
public class Plan extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "max_courses")
    private Integer maxCourses;

    @Column(name = "max_books")
    private Integer maxBooks;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "JSONB")
    private Map<String, Object> features;

    @Column(name = "is_popular", nullable = false)
    @Builder.Default
    private Boolean isPopular = false;
}
