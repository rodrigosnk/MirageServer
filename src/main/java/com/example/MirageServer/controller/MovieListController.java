package com.example.MirageServer.controller;

import com.example.MirageServer.dto.response.ErrorResponse;
import com.example.MirageServer.dto.response.SuccessResponse;
import com.example.MirageServer.services.MovieListService;
import com.example.MirageServer.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")
public class MovieListController {

    private final MovieListService movieListService;
    private final JwtUtil jwtUtil;

    public MovieListController(MovieListService movieListService, JwtUtil jwtUtil) {
        this.movieListService = movieListService;
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

    @GetMapping("/{listName}")
    public ResponseEntity<?> getMovies(@PathVariable String listName, HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            List<Long> movies = movieListService.getMovies(userId, listName);
            SuccessResponse response = new SuccessResponse("OK", "Movies retrieved successfully");
            return ResponseEntity.status(HttpStatus.OK).body(movies);
        } catch (RuntimeException e) {
            ErrorResponse response = new ErrorResponse("Not Found", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/{listName}")
    public ResponseEntity<?> addMovie(@PathVariable String listName,
                                      @RequestBody Map<String, Long> body,
                                      HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            Long movieId = body.get("movieId");
            if (movieId == null) {
                ErrorResponse response = new ErrorResponse("Bad Request", "Field 'movieId' is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            movieListService.addMovie(userId, listName, movieId);
            SuccessResponse response = new SuccessResponse("OK", "Movie added to list '" + listName + "'");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            ErrorResponse response = new ErrorResponse("Conflict", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @DeleteMapping("/{listName}/{movieId}")
    public ResponseEntity<?> deleteMovie(@PathVariable String listName,
                                         @PathVariable Long movieId,
                                         HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            movieListService.deleteMovie(userId, listName, movieId);
            SuccessResponse response = new SuccessResponse("OK", "Movie removed from list '" + listName + "'");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            ErrorResponse response = new ErrorResponse("Not Found", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{listName}")
    public ResponseEntity<?> deleteList(@PathVariable String listName, HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            movieListService.deleteList(userId, listName);
            SuccessResponse response = new SuccessResponse("OK", "List removed '" + listName + "'");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            ErrorResponse response = new ErrorResponse("Not Found", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/top-10/position")
    public ResponseEntity<?> moveTop10Position(@RequestBody Map<String, Integer> body,
                                               HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            Long movieId = Long.valueOf(body.get("movieId"));
            int position = body.get("position");
            movieListService.moveTop10Position(userId, movieId, position);
            SuccessResponse response = new SuccessResponse("OK", "Top-10 updated");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            ErrorResponse response = new ErrorResponse("Bad Request", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
