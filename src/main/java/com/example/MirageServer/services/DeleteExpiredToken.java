package com.example.MirageServer.services;


import com.example.MirageServer.repository.InvalidatedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DeleteExpiredToken  {
    private InvalidatedTokenRepository invalidatedTokenRepository;

    public DeleteExpiredToken(InvalidatedTokenRepository invalidatedTokenRepository) {
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    @Scheduled(fixedRate = 3600000)
    public void deleteExpiredTokens() {
        invalidatedTokenRepository.deleteByExpirationDateBefore(Instant.now());
    }
}
