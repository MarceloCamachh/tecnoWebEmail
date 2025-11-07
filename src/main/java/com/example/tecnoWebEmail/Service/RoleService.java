package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.Role;
import com.example.tecnoWebEmail.Repository.RoleRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public Role createRole(Role role) {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new RuntimeException("El rol ya existe: " + role.getName());
        }
        return roleRepository.save(role);
    }

    /**
     * @param currentName El nombre actual del rol a buscar.
     * @param newName     El nuevo nombre para el rol.
     * @return El rol actualizado.
     */
    @Transactional
    public Role updateRole(String currentName, String newName) {
        Role role = roleRepository.findByName(currentName)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + currentName));

        // 2. Si el nombre nuevo es el mismo, no hacer nada
        if (currentName.equalsIgnoreCase(newName)) {
            return role;
        }

        // 3. Validar que el nuevo nombre no exista ya
        if (roleRepository.findByName(newName).isPresent()) {
            throw new RuntimeException("El nuevo nombre de rol ya está en uso: " + newName);
        }

        // 4. Actualizar y guardar
        role.setName(newName);
        return roleRepository.save(role);
    }
}