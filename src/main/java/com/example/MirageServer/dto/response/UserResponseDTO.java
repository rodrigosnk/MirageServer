package com.example.MirageServer.dto.response;

import com.example.MirageServer.dto.TokenDTO;

// classe que armazena informacoes do user que serao respondidas ao cliente
public record UserResponseDTO(
        Long userId,
        String name,
        String email,
        String avatarUrl,
        TokenDTO token
) {
}
