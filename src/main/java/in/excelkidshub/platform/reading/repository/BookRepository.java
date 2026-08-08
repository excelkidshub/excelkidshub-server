package in.excelkidshub.platform.reading.repository;

import in.excelkidshub.platform.reading.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Book entity.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find active books.
     */
    List<Book> findByActiveTrue();

    /**
     * Find free books.
     */
    List<Book> findByIsFreeTrueAndActiveTrue();

    /**
     * Find books by level name.
     */
    List<Book> findByLevelNameAndActiveTrue(String levelName);

    /**
     * Find books by category.
     */
    List<Book> findByCategoryAndActiveTrue(String category);

    /**
     * Find books by age group.
     */
    List<Book> findByAgeGroupAndActiveTrue(String ageGroup);

    /**
     * Search books by title (case-insensitive).
     */
    @Query("SELECT b FROM Book b WHERE b.active = true AND LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> searchByTitle(@Param("keyword") String keyword);

    /**
     * Find books by language.
     */
    List<Book> findByLanguageAndActiveTrue(String language);
}
