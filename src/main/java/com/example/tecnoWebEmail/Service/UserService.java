package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.Client;
import com.example.tecnoWebEmail.Models.Role;
import com.example.tecnoWebEmail.Models.User;
import com.example.tecnoWebEmail.Repository.RoleRepository;
import com.example.tecnoWebEmail.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(User user, String roleName) {

        // 1. Validar si el CI, username o email ya existen
        if (userRepository.findByCi(user.getCi()).isPresent()) {
            throw new RuntimeException("CI already registered: " + user.getCi());
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken: " + user.getUsername());
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use: " + user.getEmail());
        }

        // 2. Buscar y asignar el rol
        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.setRole(userRole);

        // 3.Encriptar la contraseña antes de guardar
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // 4. Activar usuario y guardar
        user.setActive(true);
        return userRepository.save(user);
    }

    /**
     * Actualiza un usuario existente, identificado por su CI.
     * No actualiza la contraseña (eso debería ser un comando separado).
     */
    @Transactional
    public User updateUser(String ci, User userDetails, String roleName) {

        // 1. Encontrar al usuario por su CI
        User user = userRepository.findByCi(ci)
                .orElseThrow(() -> new RuntimeException("User not found with CI: " + ci));

        // 2. Buscar y asignar el nuevo ROL
        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.setRole(newRole);

        // 3. Validar y actualizar USERNAME (si es diferente)
        if (!user.getUsername().equals(userDetails.getUsername())) {
            if (userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
                throw new RuntimeException("New username is already taken: " + userDetails.getUsername());
            }
            user.setUsername(userDetails.getUsername());
        }

        // 4. Validar y actualizar EMAIL (si es diferente)
        if (!user.getEmail().equals(userDetails.getEmail())) {
            if (userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
                throw new RuntimeException("New email is already in use: " + userDetails.getEmail());
            }
            user.setEmail(userDetails.getEmail());
        }

        // 5. Actualizar el resto de campos
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());

        return userRepository.save(user);
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getUsersByRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        return userRepository.findByRole(role);
    }

    public Optional<User> getUserByCi(String ci) {
        return userRepository.findByCi(ci);
    }

}