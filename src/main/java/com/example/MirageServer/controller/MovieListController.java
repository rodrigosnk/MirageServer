package com.example.MirageServer.controller;

import com.example.MirageServer.dto.response.ErrorResponse;
import com.example.MirageServer.model.User;
import com.example.MirageServer.model.UserList;
import com.example.MirageServer.model.UserListItem;
import com.example.MirageServer.repository.UserListRepository;
import com.example.MirageServer.repository.UserRepository;
import com.example.MirageServer.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/movies")
public class MovieListController {

    private final UserListRepository userListRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public MovieListController(UserListRepository userListRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userListRepository = userListRepository;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/{listName}")
    public ResponseEntity<?> addMovie(
            @PathVariable String listName,
            @RequestBody Map<String, Long> body,
            HttpServletRequest request) {

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

        Long movieId = body.get("movieId");
        if (movieId == null) {
            ErrorResponse response = new ErrorResponse(
                    "Bad Request",
                    "Field 'movieId' is required"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        UserList list = userListRepository.findByUserAndName(user, listName)
                .orElseGet(() -> {
                    UserList newList = new UserList();
                    newList.setUser(user);
                    newList.setName(listName);
                    return newList;
                });
        boolean exists = list.getItems().stream()
                .anyMatch(item -> item.getMovieId().equals(movieId));

        if (exists) {
            ErrorResponse response = new ErrorResponse(
                    "Conflict",
                    "Movie already exists in list '" + listName + "'"
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        UserListItem item = new UserListItem();
        item.setMovieId(movieId);
        item.setList(list);
        int nextRank = list.getItems().stream()
                .mapToInt(UserListItem::getMovieRank)
                .max()
                .orElse(0) + 1;
        item.setMovieRank(nextRank);

        list.getItems().add(item);
        userListRepository.save(list);

        return ResponseEntity.ok(Map.of("message", "Movie added to list '" + listName + "'"));
    }



    @GetMapping("/{listName}")
    public ResponseEntity<?> getMovies(
            @PathVariable String listName,
            HttpServletRequest request) {

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

        UserList list = userListRepository.findByUserAndName(user, listName)
                .orElse(null);

        if (list == null &&
                (listName.equalsIgnoreCase("favorites")
                        || listName.equalsIgnoreCase("watch-later")
                        || listName.equalsIgnoreCase("top-10"))) {
            return ResponseEntity.ok(List.of()); // retorna []
        }

        if (list == null) {
            ErrorResponse response = new ErrorResponse(
                    "Not Found",
                    "List '" + listName + "' not found"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        //ordena por ranking
        List<UserListItem> sorted = list.getItems().stream()
                .sorted((a, b) -> Integer.compare(a.getMovieRank(), b.getMovieRank()))
                .toList();
        list.getItems().clear();
        list.getItems().addAll(sorted);

        List<Long> movieIds = list.getItems().stream()
                .map(UserListItem::getMovieId)
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(movieIds);
    }

    @PutMapping("/top-10/position")
    public ResponseEntity<?> moveTop10Position(
            @RequestBody Map<String, Integer> body,
            HttpServletRequest request) {

        String token = jwtUtil.resolveToken(request);
        if (token == null) {
            ErrorResponse response = new ErrorResponse(
                    "Unauthorized",
                    "Authorization token is missing or invalid"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Recupera usuario
        Claims claims = jwtUtil.parseJwtClaims(token);
        Long userId = Long.parseLong(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Extrai parâmetros
        Long movieId = Long.valueOf(body.get("movieId"));
        int position = body.get("position");

        if (position < 1 || position > 10) {
            ErrorResponse response = new ErrorResponse(
                    "Bad Request",
                    "Invalid position"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Busca lista top-10 do usuário
        UserList list = userListRepository.findByUserAndName(user, "top-10")
                .orElseThrow(() -> new RuntimeException("List 'top-10' not found"));


        // Busca o filme
        UserListItem item = list.getItems().stream()
                .filter(i -> i.getMovieId().equals(movieId))
                .findFirst()
                .orElse(null);

        if (item == null) {
            ErrorResponse response = new ErrorResponse(
                    "Not Found",
                    "Movie not found in top10"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Ordena lista atual por rank
        List<UserListItem> sorted = list.getItems().stream()
                .sorted((a, b) -> Integer.compare(a.getMovieRank(), b.getMovieRank()))
                .toList();


        // Remove item
        sorted = new java.util.ArrayList<>(sorted);
        sorted.remove(item);

        // Insere item na nova posição
        sorted.add(position - 1, item);

        // Reatribui ranks
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setMovieRank(i + 1);
            System.out.println("filme " + sorted.get(i).getMovieId() + " pos " + sorted.get(i).getMovieRank());
        }

        // Atualiza lista
        list.getItems().clear();
        list.getItems().addAll(sorted);

        userListRepository.save(list);

        // Retorna lista atualizada
        List<Long> movieIds = list.getItems().stream()
                .map(UserListItem::getMovieId)
                .toList();

        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{listName}/{movieId}")
    public ResponseEntity<?> deleteMovie(
            @PathVariable String listName,
            @PathVariable Long movieId,
            HttpServletRequest request) {

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

        UserList list = userListRepository.findByUserAndName(user, listName)
                .orElseThrow(() -> new RuntimeException("List '" + listName + "' not found"));

        UserListItem itemToRemove = list.getItems().stream()
                .filter(item -> item.getMovieId().equals(movieId))
                .findFirst()
                .orElse(null);

        if (itemToRemove == null) {
            ErrorResponse response = new ErrorResponse(
                    "Not Found",
                    "Movie with ID " + movieId + " not found in list '" + listName + "'"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        list.getItems().remove(itemToRemove);
        userListRepository.save(list);

        return ResponseEntity.ok(Map.of("message", "Movie removed from list '" + listName + "'"));
    }

    @DeleteMapping("/{listName}")
    public ResponseEntity<?> deleteList(
            @PathVariable String listName,
            HttpServletRequest request) {

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


        UserList list = userListRepository.findByUserAndName(user, listName)
                .orElseThrow(() -> new RuntimeException("List '" + listName + "' not found"));


        userListRepository.delete(list);

        return ResponseEntity.ok(Map.of("message", "List removed '" + listName + "'"));
    }
}
