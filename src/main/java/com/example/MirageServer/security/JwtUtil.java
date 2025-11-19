package com.example.MirageServer.security;

import com.example.MirageServer.dto.TokenDTO;
import com.example.MirageServer.model.InvalidatedToken;
import com.example.MirageServer.model.User;
import com.example.MirageServer.repository.InvalidatedTokenRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtil {


    private final String secretKey;
    private final long accessTokenValidity;
    private final JwtParser jwtParser;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final Key key;

    private final String TOKEN_HEADER = "Authorization";
    private final String TOKEN_PREFIX = "Bearer ";

    public JwtUtil(@Value("${jwt.secret}") String secretKey,
                   @Value("${jwt.expiration}") long accessTokenValidity,
                   InvalidatedTokenRepository invalidatedTokenRepository) {
        this.secretKey = secretKey;
        this.accessTokenValidity = accessTokenValidity;
        this.invalidatedTokenRepository = invalidatedTokenRepository;

        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
    }

    public TokenDTO createToken(User user) {
        Date tokenCreateTime = new Date();
        Date tokenValidity = new Date(tokenCreateTime.getTime() + accessTokenValidity);

        String token = Jwts.builder()
                .setSubject(user.getUserId().toString())
                .claim("email", user.getEmail())
                .setIssuedAt(tokenCreateTime)
                .setExpiration(tokenValidity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new TokenDTO(token, tokenValidity.toInstant());
    }

    public void invalidateToken(String token, Instant expiration) {
        invalidatedTokenRepository.save(new InvalidatedToken(token, expiration));
    }

    public boolean isTokenInvalidated(String token) {
        return invalidatedTokenRepository.existsByToken(token);
    }

    public Claims parseJwtClaims(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }

    public Claims resolveClaims(HttpServletRequest req) {
        try {
            String token = resolveToken(req);
            if (token != null) {
                return parseJwtClaims(token);
            }
            return null;
        } catch (ExpiredJwtException ex) {
            req.setAttribute("expired", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            req.setAttribute("invalid", ex.getMessage());
            throw ex;
        }
    }

    public String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public boolean validateClaims(Claims claims) throws AuthenticationException {
        try {
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            throw e;
        }
    }

    public String getEmail(Claims claims) {
        return claims.getSubject();
    }

    private List<String> getRoles(Claims claims) {
        return (List<String>) claims.get("roles");
    }


}
