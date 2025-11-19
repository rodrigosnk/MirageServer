package com.example.MirageServer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "user_list_items")
public class UserListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne
    @JoinColumn(name = "list_id", nullable = false)
    @JsonIgnore
    private UserList list;

    @Column(nullable = false, unique = true)
    private Long movieId;

    private int movieRank;

    public UserListItem () {}

    public UserListItem(UserList list, Long movieId, int movieRank) {
        this.list = list;
        this.movieId = movieId;
        this.movieRank = movieRank;
    }
    public UserListItem(UserList list, Long movieId) {
        this.list = list;
        this.movieId = movieId;
    }

    public UserList getList() {
        return list;
    }

    public void setList(UserList list) {
        this.list = list;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public int getMovieRank() {
        return movieRank;
    }

    public void setMovieRank(int movieRank) {
        this.movieRank = movieRank;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
