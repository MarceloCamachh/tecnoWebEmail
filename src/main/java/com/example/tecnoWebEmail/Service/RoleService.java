package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.Role;
import com.example.tecnoWebEmail.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    public Role createRole(Role role) {
        // Opcional: Validar si el rol ya existe
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new RuntimeException("Role already exists with name: " + role.getName());
        }
        return roleRepository.save(role);
    }
}