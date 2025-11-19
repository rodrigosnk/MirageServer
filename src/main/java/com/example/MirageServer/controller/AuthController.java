package com.example.MirageServer.controller;

import com.example.MirageServer.dto.response.ErrorResponse;
import com.example.MirageServer.dto.response.SuccessResponse;
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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private InvalidatedTokenRepository invalidatedTokenRepository;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          InvalidatedTokenRepository invalidatedTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    // Criar usuário, faz as validacoes do model,
    // faz o encoding da senha com Bcrypt,
    // utiliza o DTO para retornar o objeto com informacoes relevantes
    // responde token de sessao
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            ErrorResponse response = new ErrorResponse(
                    "Conflict",
                    "User already exists. Please log in instead."
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User newUser = userRepository.save(user);

        TokenDTO token = jwtUtil.createToken(newUser);

        UserResponseDTO responseDTO = new UserResponseDTO(
                newUser.getUserId(),
                newUser.getName(),
                newUser.getEmail(),
                newUser.getAvatarUrl(),
                token
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // busca o usuario atraves do email, ( o email e unico para cada usuario )
    // compara o hash da senha com o salvo no database
    // responde token de sessao

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        // Busca o usuário pelo email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);

        if (user == null) {
            ErrorResponse response = new ErrorResponse("Unauthorized", "Authentication failed. Please check your credentials.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Verifica se a senha informada corresponde ao hash salvo
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            TokenDTO token = jwtUtil.createToken(user);

            UserResponseDTO responseDTO = new UserResponseDTO(
                    user.getUserId(),
                    user.getName(),
                    user.getEmail(),
                    user.getAvatarUrl(),
                    token
            );
            return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
        } else {
            ErrorResponse response = new ErrorResponse("Unauthorized", "Authentication failed. Please check your credentials.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        if (token != null) {
            Claims claims = jwtUtil.parseJwtClaims(token);
            Instant expiration = claims.getExpiration().toInstant();

            // salva token como inválido no banco
            InvalidatedToken invalidated = new InvalidatedToken(token, expiration);
            invalidatedTokenRepository.save(invalidated);

            SuccessResponse response = new SuccessResponse("Success", "Logout successful");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        ErrorResponse response = new ErrorResponse("Bad Request", "No token found");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> login(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        if (token == null) {
            ErrorResponse response = new ErrorResponse(
                    "Unauthorized",
                    "Authorization token is missing or invalid"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Claims claims = jwtUtil.parseJwtClaims(token);
        Long userId = Long.parseLong(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserResponseMeDTO response = new UserResponseMeDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getCreated_at()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }


}
