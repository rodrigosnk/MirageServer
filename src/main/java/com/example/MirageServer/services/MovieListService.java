package com.example.MirageServer.services;

import com.example.MirageServer.model.User;
import com.example.MirageServer.model.UserList;
import com.example.MirageServer.model.UserListItem;
import com.example.MirageServer.repository.UserListRepository;
import com.example.MirageServer.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieListService {

    private final UserListRepository userListRepository;
    private final UserRepository userRepository;

    public MovieListService(UserListRepository userListRepository, UserRepository userRepository) {
        this.userListRepository = userListRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "userMovieLists", key = "#userId + '_' + #listName")
    public List<Long> getMovies(Long userId, String listName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserList list = userListRepository.findByUserAndName(user, listName)
                .orElse(null);

        if (list == null) {
            return List.of();
        }

        return list.getItems().stream()
                .sorted((a, b) -> Integer.compare(a.getMovieRank(), b.getMovieRank()))
                .map(UserListItem::getMovieId)
                .toList();
    }

    @CacheEvict(value = "userMovieLists", key = "#userId + '_' + #listName")
    public void addMovie(Long userId, String listName, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
            throw new RuntimeException("Movie already exists in list '" + listName + "'");
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
    }

    @CacheEvict(value = "userMovieLists", key = "#userId + '_' + #listName")
    public void deleteMovie(Long userId, String listName, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserList list = userListRepository.findByUserAndName(user, listName)
                .orElseThrow(() -> new RuntimeException("List '" + listName + "' not found"));

        UserListItem itemToRemove = list.getItems().stream()
                .filter(item -> item.getMovieId().equals(movieId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Movie not found in list"));

        list.getItems().remove(itemToRemove);
        userListRepository.save(list);
    }

    @CacheEvict(value = "userMovieLists", key = "#userId + '_top-10'")
    public void moveTop10Position(Long userId, Long movieId, int position) {
        if (position < 1 || position > 10) {
            throw new RuntimeException("Invalid position");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserList list = userListRepository.findByUserAndName(user, "top-10")
                .orElseThrow(() -> new RuntimeException("List 'top-10' not found"));

        UserListItem item = list.getItems().stream()
                .filter(i -> i.getMovieId().equals(movieId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Movie not found in top-10"));

        List<UserListItem> sorted = list.getItems().stream()
                .sorted((a, b) -> Integer.compare(a.getMovieRank(), b.getMovieRank()))
                .toList();

        sorted = new java.util.ArrayList<>(sorted);
        sorted.remove(item);
        sorted.add(position - 1, item);

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setMovieRank(i + 1);
        }

        list.getItems().clear();
        list.getItems().addAll(sorted);
        userListRepository.save(list);
    }

    @CacheEvict(value = "userMovieLists", key = "#userId + '_' + #listName")
    public void deleteList(Long userId, String listName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserList list = userListRepository.findByUserAndName(user, listName)
                .orElseThrow(() -> new RuntimeException("List '" + listName + "' not found"));

        userListRepository.delete(list);
    }
}


