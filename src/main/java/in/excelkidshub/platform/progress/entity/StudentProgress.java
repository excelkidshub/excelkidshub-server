package in.excelkidshub.platform.progress.entity;

import in.excelkidshub.platform.common.entity.BaseEntity;
import in.excelkidshub.platform.course.entity.Course;
import in.excelkidshub.platform.reading.entity.Book;
import in.excelkidshub.platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * StudentProgress entity representing student learning progress.
 * Tracks progress for both courses and books.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "student_progress", indexes = {
    @Index(name = "idx_student_progress_user_id", columnList = "user_id"),
    @Index(name = "idx_student_progress_course_id", columnList = "course_id"),
    @Index(name = "idx_student_progress_book_id", columnList = "book_id"),
    @Index(name = "idx_student_progress_status", columnList = "status"),
    @Index(name = "idx_student_progress_last_accessed", columnList = "last_accessed")
})
public class StudentProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_student_progress_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", foreignKey = @ForeignKey(name = "fk_student_progress_course"))
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_student_progress_book"))
    private Book book;

    @Column(name = "lesson_number")
    private Integer lessonNumber;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "completion_percentage")
    @Builder.Default
    private Integer completionPercentage = 0;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "time_spent_minutes")
    @Builder.Default
    private Integer timeSpentMinutes = 0;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "IN_PROGRESS";
}
