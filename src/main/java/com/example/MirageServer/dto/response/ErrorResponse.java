package com.example.MirageServer.dto.response;

public record ErrorResponse(
        String error,
        String message
) {}
