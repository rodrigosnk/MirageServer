package com.example.MirageServer.security;

import com.example.MirageServer.repository.InvalidatedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@Component
public class JwtAuthorizationFilter  extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public JwtAuthorizationFilter(JwtUtil jwtUtil, InvalidatedTokenRepository invalidatedTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // ignora rotas de autenticação
        return path.startsWith("/auth");
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {

        String token = jwtUtil.resolveToken(request);

        if (token != null) {
            try {
                if (invalidatedTokenRepository.existsByToken(token)) {
                    throw new AuthenticationServiceException("Token invalidated");
                }
                Claims claims = jwtUtil.parseJwtClaims(token);

                if (jwtUtil.validateClaims(claims)) {
                    String email = claims.getSubject();

                    // Aqui você pode criar um Authentication com as roles
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, List.of());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid or expired token, authorization denied.\"}");
                return;
            }

        }

        filterChain.doFilter(request, response);
    }
}
