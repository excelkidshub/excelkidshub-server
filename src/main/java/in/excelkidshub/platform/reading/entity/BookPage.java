package in.excelkidshub.platform.reading.entity;

import in.excelkidshub.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * BookPage entity representing individual pages in a book.
 * Contains content, images, audio, and interactive elements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "book_pages", indexes = {
    @Index(name = "idx_book_pages_book_id", columnList = "book_id"),
    @Index(name = "idx_book_pages_page_number", columnList = "page_number")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_book_pages_book_page", columnNames = {"book_id", "page_number"})
})
public class BookPage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_book_pages_book"))
    private Book book;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interactive_elements", columnDefinition = "JSONB")
    private Map<String, Object> interactiveElements;
}
