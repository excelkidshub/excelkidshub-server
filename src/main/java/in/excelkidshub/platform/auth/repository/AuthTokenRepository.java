package in.excelkidshub.platform.auth.repository;

import in.excelkidshub.platform.auth.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByToken(String token);

    /** Invalidate all previous tokens of a given type for a user before issuing a new one. */
    @Modifying
    @Query("UPDATE AuthToken t SET t.used = true WHERE t.user.id = :userId AND t.tokenType = :type AND t.used = false")
    void invalidatePreviousTokens(@Param("userId") Long userId, @Param("type") String type);
}
