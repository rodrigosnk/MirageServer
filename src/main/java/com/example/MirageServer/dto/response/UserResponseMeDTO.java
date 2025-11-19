package com.example.MirageServer.dto.response;

import java.time.Instant;
import java.util.Date;

public record UserResponseMeDTO (Long id, String name, String email, Date Created_at) {
}
