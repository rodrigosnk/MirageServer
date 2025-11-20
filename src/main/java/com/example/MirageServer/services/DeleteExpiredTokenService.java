package com.example.MirageServer.services;


import com.example.MirageServer.repository.InvalidatedTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DeleteExpiredTokenService {
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public DeleteExpiredTokenService(InvalidatedTokenRepository invalidatedTokenRepository) {
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void deleteExpiredTokens() {
        invalidatedTokenRepository.deleteByExpirationDateBefore(Instant.now());
    }
}
