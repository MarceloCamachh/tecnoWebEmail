package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.Role;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleCommand {

    @Autowired
    private RoleService roleService;

    @Autowired
    private EmailResponseService emailResponseService;

    private static final String MINI_SEPARATOR = "------------------------";

    /**
     * Maneja el comando LISROL
     */
    public String handleListRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return formatListRolesResponse(roles, "LISROL");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar roles: " + e.getMessage(), "LISROL");
        }
    }

    /**
     * Maneja el comando INSROL ["nombre"]
     */
    public String handleInsertRole(String[] parameters) {
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [nombre] faltante o vacío.", "INSROL");
            }
            // Guardar roles en mayúsculas es una buena práctica
            String roleName = parameters[0].trim().toUpperCase();

            Role newRole = new Role(roleName);
            Role createdRole = roleService.createRole(newRole);

            return formatSingleRoleResponse(createdRole, "ROL CREADO EXITOSAMENTE", "INSROL");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al crear el rol: " + e.getMessage(), "INSROL");
        }
    }

    /**
     * Maneja el comando BUSROL ["nombre"]
     */
    public String handleSearchRole(String[] parameters) {
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [nombre] faltante o vacío.", "BUSROL");
            }
            String roleName = parameters[0].trim().toUpperCase();

            Role role = roleService.getRoleByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));

            return formatSingleRoleResponse(role, "ROL ENCONTRADO", "BUSROL");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al buscar el rol: " + e.getMessage(), "BUSROL");
        }
    }

    /**
     * Maneja el comando UPDROL ["nombreActual", "nombreNuevo"]
     */
    public String handleUpdateRole(String[] parameters) {
        try {
            if (parameters.length < 2) {
                return emailResponseService.formatErrorResponse("Se requieren 2 parámetros: [nombreActual, nombreNuevo].", "UPDROL");
            }

            String currentName = parameters[0].trim().toUpperCase();
            String newName = parameters[1].trim().toUpperCase();

            if (currentName.isEmpty() || newName.isEmpty()) {
                return emailResponseService.formatErrorResponse("Los nombres de rol no pueden estar vacíos.", "UPDROL");
            }

            Role updatedRole = roleService.updateRole(currentName, newName);

            return formatSingleRoleResponse(updatedRole, "ROL ACTUALIZADO EXITOSAMENTE", "UPDROL");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al actualizar el rol: " + e.getMessage(), "UPDROL");
        }
    }

    // --- Helpers de Formato de Respuesta ---

    /**
     * Formatea una respuesta para un solo rol (usado por INS, BUS, UPD)
     */
    private String formatSingleRoleResponse(Role role, String message, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));
        response.append(" ").append(message).append("\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("   • ID (Interno): ").append(role.getId()).append("\n");
        response.append("   • Nombre: ").append(role.getName()).append("\n");
        response.append("\nOK\n");
        return response.toString();
    }

    /**
     * Formatea una respuesta para la lista de roles (usado por LISROL)
     */
    private String formatListRolesResponse(List<Role> roles, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));

        if (roles.isEmpty()) {
            response.append("RESULTADO DEL LISTADO\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append(" No se encontraron roles en la base de datos.\n");
        } else {
            response.append(" LISTADO DE ROLES\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append("Total de registros encontrados: ").append(roles.size()).append("\n\n");

            int contador = 1;
            for (Role role : roles) {
                response.append(" ROL #").append(contador).append("\n");
                response.append("   • ID: ").append(role.getId()).append("\n");
                response.append("   • Nombre: ").append(role.getName()).append("\n\n");
                contador++;
            }
        }
        response.append("OK\n");
        return response.toString();
    }
}