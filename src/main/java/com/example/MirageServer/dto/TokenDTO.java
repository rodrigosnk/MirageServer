package com.example.MirageServer.dto;

import java.time.Instant;

public record TokenDTO (
        String token,
        Instant expirationDateUTC
) {}
