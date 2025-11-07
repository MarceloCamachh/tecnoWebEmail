package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Service.EmailResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommandProcessor {

    @Autowired
    private EmailResponseService emailResponseService;
    @Autowired
    private ClientCommand clientCommand;
    @Autowired
    private RoleCommand roleCommand;
    @Autowired
    private UserCommand userCommand;
    @Autowired
    private OrderCommand orderCommand;

    public String processCommand(String subject, String senderEmail) {
        System.out.println("DEBUG: processCommand iniciado - subject: [" + subject + "], sender: [" + senderEmail + "]");
        try {
            if (subject == null || subject.trim().isEmpty()) {
                return "Error: Comando vacío. Formato esperado: COMANDO[\"parametros\"]";
            }

            // Identificar el comando
            System.out.println("DEBUG: Extrayendo comando...");
            String command = extractCommand(subject);
            System.out.println("DEBUG: Comando extraído: [" + command + "]");
            String[] parameters = extractParameters(subject);
            System.out.println("DEBUG: Parámetros extraídos: " + parameters.length);

            System.out.println("DEBUG: Entrando al switch con comando: [" + command.toUpperCase() + "]");
            switch (command.toUpperCase()) {
                //cliente
                case "LISCLI":
                    return clientCommand.handleListClientes();
                case "INSCLI":
                    return clientCommand.handleInsertClient(parameters);
                case "BUSCLI":
                    return clientCommand.handleFindClientByCI(parameters);//por ci

                //roles
                case "LISROL":
                    return roleCommand.handleListRoles();
                case "INSROL":
                    return roleCommand.handleInsertRole(parameters);
                case "BUSROL":
                    return roleCommand.handleSearchRole(parameters);//por nombre
                case "UPDROL":
                    return roleCommand.handleUpdateRole(parameters);

                //users
                case "LISUSU":
                    return userCommand.handleListUsers();
                case "BUSUSU":
                    return userCommand.handleSearchUserByCi(parameters);//por ci
                case "INSUSU":
                    return userCommand.handleInsertUser(parameters);
                case "UPDUSU":
                    return userCommand.handleUpdateUser(parameters);

                //orders
                case "LISORD":
                    return orderCommand.handleListOrders();
                case "BUSORD":
                    return orderCommand.handleSearchOrder(parameters);//por id
                case "INSORD":
                    return orderCommand.handleInsertOrder(parameters);
                default:
                    return emailResponseService.formatUnknownCommandResponse(command);
            }

        } catch (Exception e) {
            return emailResponseService.formatErrorResponse(e.getMessage(), subject);
        }
    }
     private String extractCommand(String subject) {
        Pattern pattern = Pattern.compile("^([A-Z]+)\\[");
        Matcher matcher = pattern.matcher(subject.trim());
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Formato de comando inválido. Use: COMANDO[\"parametros\"]");
    }
     private String[] extractParameters(String subject) {
        System.out.println("extractParameters - Subject recibido: [" + subject + "]");
        System.out.println("extractParameters - Longitud del subject: " + subject.length());
        
        Pattern pattern = Pattern.compile("\\[(.*)\\]");
        Matcher matcher = pattern.matcher(subject.trim());
        if (matcher.find()) {
            String paramString = matcher.group(1);
            System.out.println("extractParameters - Contenido entre corchetes: [" + paramString + "]");
            
            // Caso especial para el parámetro "*"
            if (paramString.equals("*") || paramString.equals("\"*\"")) {
                return new String[]{"*"};
            }
            
            // Verificar si el contenido parece estar cortado (no termina con comillas)
            if (paramString.contains("\"") && !paramString.trim().endsWith("\"")) {
                System.out.println("extractParameters - ADVERTENCIA: El contenido parece estar cortado");
                System.out.println("extractParameters - Último carácter: [" + paramString.charAt(paramString.length()-1) + "]");
            }
            
            // Si hay múltiples parámetros separados por comas
            if (paramString.contains(",")) {
                System.out.println("extractParameters - Detectados múltiples parámetros");
                
                // Usar regex para extraer parámetros entre comillas correctamente
                Pattern paramPattern = Pattern.compile("\"([^\"]*?)\"");
                Matcher paramMatcher = paramPattern.matcher(paramString);
                
                List<String> params = new ArrayList<>();
                while (paramMatcher.find()) {
                    params.add(paramMatcher.group(1));
                }
                
                System.out.println("extractParameters - Parámetros encontrados con regex: " + params.size());
                for (int i = 0; i < params.size(); i++) {
                    System.out.println("extractParameters - Parámetro " + i + ": [" + params.get(i) + "]");
                }
                
                return params.toArray(new String[0]);
            } else {
                // Un solo parámetro
                if (paramString.startsWith("\"") && paramString.endsWith("\"")) {
                    paramString = paramString.substring(1, paramString.length() - 1);
                }
                return new String[]{paramString};
            }
        }
        System.out.println("extractParameters - No se encontraron parámetros");
        return new String[0];
    }
}
