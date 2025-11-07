package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.User;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCommand {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailResponseService emailResponseService;

    private static final String MINI_SEPARATOR = "------------------------";

    /**
     * Maneja el comando LISUSU
     */
    public String handleListUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return formatListUserResponse(users, "LISUSU");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar usuarios: " + e.getMessage(), "LISUSU");
        }
    }

    /**
     * Maneja el comando BUSUSU ["ci"]
     */
    public String handleSearchUserByCi(String[] parameters) {
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [ci] faltante o vacío.", "BUSUSU");
            }
            String ci = parameters[0].trim();

            User user = userService.getUserByCi(ci)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con CI: " + ci));

            return formatSingleUserResponse(user, "USUARIO ENCONTRADO", "BUSUSU");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al buscar usuario: " + e.getMessage(), "BUSUSU");
        }
    }

    /**
     * Maneja el comando INSUSU ["ci", "username", "password", "email", "firstName", "lastName", "roleName"]
     */
    public String handleInsertUser(String[] parameters) {
        try {
            // 7 parámetros requeridos
            if (parameters.length < 7) {
                return emailResponseService.formatErrorResponse(
                        "Número incorrecto de parámetros. Se esperaban 7: [ci, username, password, email, firstName, lastName, roleName]",
                        "INSUSU");
            }

            // Validar campos obligatorios
            if (parameters[0].trim().isEmpty() || parameters[1].trim().isEmpty() ||
                    parameters[2].trim().isEmpty() || parameters[3].trim().isEmpty() ||
                    parameters[6].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Los campos [ci, username, password, email, roleName] no pueden estar vacíos.", "INSUSU");
            }

            // Crear el objeto User con los parámetros
            User newUser = new User();
            newUser.setCi(parameters[0].trim());
            newUser.setUsername(parameters[1].trim());
            newUser.setPassword(parameters[2]);
            newUser.setEmail(parameters[3].trim());
            newUser.setFirstName(paramToNull(parameters[4]));
            newUser.setLastName(paramToNull(parameters[5]));
            String roleName = parameters[6].trim().toUpperCase();

            // Llamar al servicio
            User createdUser = userService.createUser(newUser, roleName);

            return formatSingleUserResponse(createdUser, "USUARIO CREADO EXITOSAMENTE", "INSUSU");

        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al crear usuario: " + e.getMessage(), "INSUSU");
        }
    }

    /**
     * Maneja el comando UPDUSU ["ci_a_buscar", "nuevo_username", "nuevo_email", "nuevo_firstName", "nuevo_lastName", "nuevo_rolName"]
     */
    public String handleUpdateUser(String[] parameters) {
        try {
            // 6 parámetros requeridos
            if (parameters.length < 6) {
                return emailResponseService.formatErrorResponse(
                        "Número incorrecto de parámetros. Se esperaban 6: [ci_a_buscar, nuevo_username, nuevo_email, nuevo_firstName, nuevo_lastName, nuevo_rolName]",
                        "UPDUSU");
            }

            String ci = parameters[0].trim();
            if (ci.isEmpty()) {
                return emailResponseService.formatErrorResponse("El [ci_a_buscar] no puede estar vacío.", "UPDUSU");
            }

            // Crear un objeto 'User' temporal con los detalles a cambiar
            User userDetails = new User();
            userDetails.setUsername(parameters[1].trim());
            userDetails.setEmail(parameters[2].trim());
            userDetails.setFirstName(paramToNull(parameters[3]));
            userDetails.setLastName(paramToNull(parameters[4]));
            String roleName = parameters[5].trim().toUpperCase();

            // Validar campos obligatorios
            if (userDetails.getUsername().isEmpty() || userDetails.getEmail().isEmpty() || roleName.isEmpty()) {
                return emailResponseService.formatErrorResponse("Los campos [nuevo_username, nuevo_email, nuevo_rolName] no pueden estar vacíos.", "UPDUSU");
            }

            User updatedUser = userService.updateUser(ci, userDetails, roleName);

            return formatSingleUserResponse(updatedUser, "USUARIO ACTUALIZADO EXITOSAMENTE", "UPDUSU");

        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al actualizar usuario: " + e.getMessage(), "UPDUSU");
        }
    }

    // --- Helpers ---

    private String paramToNull(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        return param.trim();
    }

    private String formatSingleUserResponse(User user, String message, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));
        response.append(" ").append(message).append("\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("   • ID (Interno): ").append(user.getId()).append("\n");
        response.append("   • CI: ").append(user.getCi()).append("\n");
        response.append("   • Username: ").append(user.getUsername()).append("\n");
        response.append("   • Nombre: ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n");
        response.append("   • Email: ").append(user.getEmail()).append("\n");
        response.append("   • Rol: ").append(user.getRole().getName()).append("\n");
        response.append("   • Activo: ").append(user.isActive()).append("\n");
        response.append("\nOK\n");
        return response.toString();
    }

    private String formatListUserResponse(List<User> users, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));

        if (users.isEmpty()) {
            response.append("RESULTADO DEL LISTADO\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append(" No se encontraron usuarios en la base de datos.\n");
        } else {
            response.append(" LISTADO DE USUARIOS\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append("Total de registros encontrados: ").append(users.size()).append("\n\n");

            int contador = 1;
            for (User user : users) {
                response.append(" USUARIO #").append(contador).append("\n");
                response.append("   • CI: ").append(user.getCi()).append("\n");
                response.append("   • Username: ").append(user.getUsername()).append("\n");
                response.append("   • Nombre: ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n");
                response.append("   • Email: ").append(user.getEmail()).append("\n");
                response.append("   • Rol: ").append(user.getRole().getName()).append("\n");
                response.append("   • Activo: ").append(user.isActive()).append("\n\n");
                contador++;
            }
        }
        response.append("OK\n");
        return response.toString();
    }
}