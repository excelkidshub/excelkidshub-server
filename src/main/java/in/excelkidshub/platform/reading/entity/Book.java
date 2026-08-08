package in.excelkidshub.platform.reading.entity;

import in.excelkidshub.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Book entity representing reading books.
 * Supports unlimited books with generic level names (not hardcoded).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_books_title", columnList = "title"),
    @Index(name = "idx_books_level_name", columnList = "level_name"),
    @Index(name = "idx_books_category", columnList = "category"),
    @Index(name = "idx_books_age_group", columnList = "age_group"),
    @Index(name = "idx_books_active", columnList = "active"),
    @Index(name = "idx_books_is_free", columnList = "is_free")
})
public class Book extends BaseEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "author", length = 255)
    private String author;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "level_name", nullable = false, length = 100)
    private String levelName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "total_pages", nullable = false)
    private Integer totalPages;

    @Column(name = "reading_time_minutes")
    private Integer readingTimeMinutes;

    @Column(name = "age_group", length = 50)
    private String ageGroup;

    @Column(name = "language", length = 50)
    @Builder.Default
    private String language = "English";

    @Column(name = "is_free", nullable = false)
    @Builder.Default
    private Boolean isFree = false;
}
