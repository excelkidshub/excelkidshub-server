package in.excelkidshub.platform.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for JWT token generation, validation, and claim extraction.
 * Uses HS256 signing with a minimum 256-bit secret loaded from application properties.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    // ── Token Generation ──────────────────────────────────────────────────────

    /**
     * Generate a signed JWT token for the given user.
     *
     * @param userId    database ID of the user
     * @param email     user's email address (subject)
     * @param role      user's role name (e.g. STUDENT, ADMIN)
     * @return signed JWT string
     */
    public String generateToken(Long userId, String email, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Validate a JWT token. Returns true only if the token is structurally valid,
     * correctly signed, and not expired.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT signature invalid");
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported");
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims empty");
        }
        return false;
    }

    // ── Claim Extraction ──────────────────────────────────────────────────────

    /**
     * Extract the user ID (subject claim) from a validated token.
     */
    public Long extractUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /**
     * Extract the email claim from a validated token.
     */
    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    /**
     * Extract the role claim from a validated token.
     */
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Check whether a token has expired (used in filter to distinguish
     * expired vs invalid tokens for clearer error responses).
     */
    public boolean isTokenExpired(String token) {
        try {
            parseClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
