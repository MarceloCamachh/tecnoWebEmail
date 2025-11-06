package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Role;
import com.example.tecnoWebEmail.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar usuario por username (para login)
    Optional<User> findByUsername(String username);

    // Buscar usuario por email (para validación)
    Optional<User> findByEmail(String email);

    // Buscar todos los usuarios que tienen un rol específico
    List<User> findByRole(Role role);

    // Contar usuarios por rol
    long countByRole(Role role);
}
