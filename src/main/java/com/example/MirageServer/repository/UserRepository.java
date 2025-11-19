package com.example.MirageServer.repository;

import com.example.MirageServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Buscar usuário pelo email
    Optional<User> findByEmail(String email);

    // Verificar se já existe email cadastrado
    boolean existsByEmail(String email);
}
