package com.example.MirageServer.repository;

import com.example.MirageServer.model.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {
    Optional<InvalidatedToken> findByToken(String token);
    boolean existsByToken(String token);

    void deleteByExpirationDateBefore(Instant now);


}
