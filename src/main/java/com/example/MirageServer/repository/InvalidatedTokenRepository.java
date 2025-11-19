package com.example.MirageServer.repository;

import com.example.MirageServer.model.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {
    Optional<InvalidatedToken> findByToken(String token);
    boolean existsByToken(String token);
}
