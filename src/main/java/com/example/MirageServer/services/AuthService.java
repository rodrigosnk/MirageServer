package com.example.MirageServer.services;

import com.example.MirageServer.dto.LoginRequestDTO;
import com.example.MirageServer.dto.TokenDTO;
import com.example.MirageServer.dto.response.UserResponseDTO;
import com.example.MirageServer.dto.response.UserResponseMeDTO;
import com.example.MirageServer.model.InvalidatedToken;
import com.example.MirageServer.model.User;
import com.example.MirageServer.repository.InvalidatedTokenRepository;
import com.example.MirageServer.repository.UserRepository;
import com.example.MirageServer.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       InvalidatedTokenRepository invalidatedTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    public UserResponseDTO register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User already exists. Please log in instead.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User newUser = userRepository.save(user);

        TokenDTO token = jwtUtil.createToken(newUser);

        return new UserResponseDTO(
                newUser.getUserId(),
                newUser.getName(),
                newUser.getEmail(),
                newUser.getAvatarUrl(),
                token
        );
    }

    public UserResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Authentication failed. Please check your credentials."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Authentication failed. Please check your credentials.");
        }

        TokenDTO token = jwtUtil.createToken(user);

        return new UserResponseDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                token
        );
    }

    public void logout(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        if (token == null) {
            throw new RuntimeException("No token found");
        }

        Claims claims = jwtUtil.parseJwtClaims(token);
        Instant expiration = claims.getExpiration().toInstant();

        InvalidatedToken invalidated = new InvalidatedToken(token, expiration);
        invalidatedTokenRepository.save(invalidated);
    }

    @Cacheable(value = "userMe", key = "#userId")
    public UserResponseMeDTO me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponseMeDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getCreated_at()
        );
    }
}
