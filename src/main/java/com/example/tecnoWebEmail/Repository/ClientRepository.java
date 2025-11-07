package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    // Buscar cliente por email
    Optional<Client> findByEmail(String email);

    // Buscar cliente por número de teléfono
    Optional<Client> findByPhone(String phone);

    // Buscar clientes por apellido (puede devolver varios)
    List<Client> findByLastNameContainingIgnoreCase(String lastName);

    // Buscar clientes por nombre o apellido
    List<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}
