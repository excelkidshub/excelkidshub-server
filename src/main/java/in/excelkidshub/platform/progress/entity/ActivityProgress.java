package in.excelkidshub.platform.progress.entity;

import in.excelkidshub.platform.common.entity.BaseEntity;
import in.excelkidshub.platform.course.entity.Course;
import in.excelkidshub.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ActivityProgress entity for tracking practice activities, games, and assessments.
 * These do not have page numbers - they are tracked by activity type and ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "activity_progress")
public class ActivityProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_activity_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", foreignKey = @ForeignKey(name = "fk_activity_course"))
    private Course course;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;  // PRACTICE, GAME, ASSESSMENT, SONGS

    @Column(name = "activity_id", nullable = false, length = 100)
    private String activityId;    // e.g. "sound-match-group-1", "word-builder-s"

    @Column(name = "score")
    private Integer score;       // 0-100, nullable

    @Column(name = "completed")
    @Builder.Default
    private Boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
