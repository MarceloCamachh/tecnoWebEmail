package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.Client;
import com.example.tecnoWebEmail.Service.ClientService;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ClientCommand {
    @Autowired
    private ClientService clientService;
    @Autowired
    private EmailResponseService emailResponseService;
    private static final String MINI_SEPARATOR = "------------------------";


    public String handleListClientes() {
        try {
            List<Client> clients = clientService.getAllClients();
            return formatListClientsResponse(clients, "LISPER");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar personas: " + e.getMessage(), "LISCLI");

        }
    }

    public String handleFindClientByCI(String[] parameters){
        try {
            String ci = parameters[0];
            if (ci.isEmpty()){
                return emailResponseService.formatErrorResponse("El CI introducido es invalido","BUSCLI");
            }
            Client client = clientService.getClientByCi(ci)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con CI: " + ci));
            return formatClientResponse(client,"BUSCLI");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar personas: " + e.getMessage(), "BUSCLI");
        }
    }

    public String handleInsertClient(String[] parameters) {
        try {
            // El modelo Client tiene 6 campos (ci, firstName, lastName, email, phone, address)
            if (parameters.length < 6) {
                return emailResponseService.formatErrorResponse(
                        "Número incorrecto de parámetros. Se esperaban 6: [ci, nombre, apellido, email, telefono, direccion]",
                        "INSCLI");
            }

            // 2. Validar campos obligatorios (según tu entidad Client)
            String ci = parameters[0];
            String firstName = parameters[1];

            if (ci == null || ci.trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("El parámetro CI no puede estar vacío.", "INSCLI");
            }
            if (firstName == null || firstName.trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("El parámetro Nombres no puede estar vacío.", "INSCLI");
            }

            // 3. Crear el objeto Cliente
            // Esto convierte "" en null, lo cual es mejor para la BD (especialmente con constraints 'UNIQUE').
            Client newClient = new Client();
            newClient.setCi(ci.trim());
            newClient.setFirstName(firstName.trim());
            newClient.setLastName(paramToNull(parameters[2]));
            newClient.setEmail(paramToNull(parameters[3]));
            newClient.setPhone(paramToNull(parameters[4]));
            newClient.setAddress(paramToNull(parameters[5]));

            // 4. Llamar al servicio para crear el cliente
            Client createdClient = clientService.createClient(newClient);

            // 5. Retornar la respuesta de éxito formateada
            return formatClientResponse(createdClient, "INSCLI");

        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al crear el cliente: " + e.getMessage(), "INSCLI");
        }
    }

    public String formatListClientsResponse(List<Client> clients, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));

        if (clients.isEmpty()) {
            response.append("RESULTADO DEL LISTADO\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append(" No se encontraron cliens en la base de datos.\n");
        } else {
            response.append(" LISTADO DE PERSONAS\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append("Total de registros encontrados: ").append(clients.size()).append("\n\n");

            int contador = 1;
            for (Client client : clients) {
                response.append(" PERSONA #").append(contador).append("\n");
                response.append("   • CI: ").append(client.getId()).append("\n");
                response.append("   • Nombre: ").append(client.getFirstName()).append(" ").append(client.getLastName()).append("\n");
                response.append("   • Direccion: ").append(client.getAddress()).append("\n");
                response.append("   • Teléfono: ").append(client.getPhone()).append("\n");
                response.append("   • Email: ").append(client.getEmail()).append("\n");
                response.append("\n");
                contador++;
            }
        }
        System.out.println(response.toString());
        return response.toString();
    }
    private String paramToNull(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        return param.trim();
    }

    private String formatClientResponse(Client client, String command) {
        StringBuilder response = new StringBuilder();
        String type = "ENCONTRADO";
        if (Objects.equals(command, "INSCLI")) type = "CREADO";
        response.append(emailResponseService.generateHeader(command));
        response.append(" CLIENTE ").append(type).append(" EXITOSAMENTE\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("   • ID (Interno): ").append(client.getId()).append("\n");
        response.append("   • CI: ").append(client.getCi()).append("\n");
        response.append("   • Nombre: ").append(client.getFirstName()).append(" ").append(client.getLastName()).append("\n");
        response.append("   • Email: ").append(client.getEmail()).append("\n");
        response.append("   • Teléfono: ").append(client.getPhone()).append("\n");
        response.append("   • Direccion: ").append(client.getAddress()).append("\n");
        response.append("\n");
        response.append("OK\n");
        return response.toString();
    }

}
