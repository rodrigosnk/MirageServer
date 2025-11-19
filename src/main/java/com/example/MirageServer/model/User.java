package com.example.MirageServer.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;


    @NotBlank(message = "The name cannot be empty.")
    @Size(min = 3, max = 100, message = "The name must be longer than 3 and shorter than 100.")
    @Column(nullable = false, length = 100)
    private String name;


    @Email(message = "Email is not valid", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    @NotBlank(message = "The email cannot be empty.")
    @Size(min = 3, max = 150, message = "The email must be between 3 and 150 characters.")
    @Column(nullable = false, unique = true, length = 150)
    private String email;


    @NotBlank(message = "The password cannot be empty.")
    @Size(min = 8, max = 64, message = "The password must be between 8 and 64 characters.")
    @Column(nullable = false)
    private String password;


    @Column()
    private String avatarUrl;


    @CreationTimestamp
    @Column(updatable = false)
    private Date created_at;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserList> lists = new ArrayList<>();


    public User() {}

    public User(String name, String email, String password, String avatarUrl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.avatarUrl = avatarUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public List<UserList> getLists() {
        return lists;
    }

    public void setLists(List<UserList> lists) {
        this.lists = lists;
    }
}
