package com.example.MirageServer.repository;

import com.example.MirageServer.model.User;
import com.example.MirageServer.model.UserList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserListRepository extends JpaRepository<UserList, Long> {
    Optional<UserList> findByUserAndName(User user, String name);
}
