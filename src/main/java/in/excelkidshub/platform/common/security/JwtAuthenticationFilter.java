package in.excelkidshub.platform.common.security;

import in.excelkidshub.platform.common.constants.ApiConstants;
import in.excelkidshub.platform.user.entity.User;
import in.excelkidshub.platform.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * JWT authentication filter. Runs once per request.
 *
 * Flow:
 *  1. Extract Bearer token from Authorization header.
 *  2. Validate signature and expiry via JwtUtil.
 *  3. Load the user from DB to confirm they still exist and are active.
 *  4. Set authentication in SecurityContext so downstream code can trust it.
 *
 * If any step fails the request continues unauthenticated — Spring Security
 * will then reject it if the endpoint requires authentication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.extractUserId(token);
                    String role  = jwtUtil.extractRole(token);

                    // Confirm user still exists and is active in DB
                    // Use JOIN FETCH to eagerly load role — avoids LazyInitializationException
                    // when Spring calls User.toString() outside a session after request completes.
                    Optional<User> userOpt = userRepository.findByIdWithRole(userId);
                    if (userOpt.isPresent() && Boolean.TRUE.equals(userOpt.get().getActive())) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userOpt.get(),
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                                );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Authenticated user id={} role={}", userId, role);
                    } else {
                        log.debug("Token valid but user id={} not found or inactive", userId);
                    }
                }
            } catch (Exception e) {
                log.error("JWT filter error: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract the raw JWT string from the Authorization: Bearer <token> header.
     * Returns null if the header is absent or not a Bearer token.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(ApiConstants.AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(ApiConstants.BEARER_PREFIX)) {
            return header.substring(ApiConstants.BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
