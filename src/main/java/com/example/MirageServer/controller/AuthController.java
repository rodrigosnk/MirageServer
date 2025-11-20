package com.example.MirageServer.controller;

import com.example.MirageServer.dto.LoginRequestDTO;
import com.example.MirageServer.dto.response.ErrorResponse;
import com.example.MirageServer.dto.response.SuccessResponse;
import com.example.MirageServer.dto.response.UserResponseDTO;
import com.example.MirageServer.dto.response.UserResponseMeDTO;
import com.example.MirageServer.model.User;
import com.example.MirageServer.security.JwtUtil;
import com.example.MirageServer.services.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        if (token == null) {
            throw new RuntimeException("No token found");
        }
        Claims claims = jwtUtil.parseJwtClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            UserResponseDTO responseDTO = authService.register(user);
            SuccessResponse response = new SuccessResponse("Created", "User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (RuntimeException e) {
            ErrorResponse response = new ErrorResponse("Conflict", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            UserResponseDTO responseDTO = authService.login(loginRequest);
            SuccessResponse response = new SuccessResponse("OK", "Login successful");
            return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
        } catch (RuntimeException e) {
            ErrorResponse response = new ErrorResponse("Unauthorized", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            authService.logout(request);
            SuccessResponse response = new SuccessResponse("OK", "Logout successful");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            ErrorResponse response = new ErrorResponse("Bad Request", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            UserResponseMeDTO responseDTO = authService.me(userId);
            return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
        } catch (RuntimeException e) {
            ErrorResponse response = new ErrorResponse("Unauthorized", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
