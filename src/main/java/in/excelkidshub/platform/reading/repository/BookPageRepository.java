package in.excelkidshub.platform.reading.repository;

import in.excelkidshub.platform.reading.entity.BookPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for BookPage entity.
 */
@Repository
public interface BookPageRepository extends JpaRepository<BookPage, Long> {

    /**
     * Find pages by book ID ordered by page number.
     */
    List<BookPage> findByBookIdOrderByPageNumberAsc(Long bookId);

    /**
     * Find page by book ID and page number.
     */
    BookPage findByBookIdAndPageNumber(Long bookId, Integer pageNumber);

    /**
     * Count pages by book ID.
     */
    long countByBookId(Long bookId);

    /**
     * Find active pages by book ID.
     */
    List<BookPage> findByBookIdAndActiveTrueOrderByPageNumberAsc(Long bookId);
}
