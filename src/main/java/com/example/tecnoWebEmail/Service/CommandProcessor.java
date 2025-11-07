package com.example.tecnoWebEmail.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tecnoWebEmail.Models.Client;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommandProcessor {

    @Autowired
    private ClientService ClientService;

    @Autowired
    private EmailResponseService emailResponseService;

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
                case "LISPER":
                    return handleListClientes(parameters);
                
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

    private String handleListClientes(String[] parameters) {
        try {
            List<Client> clients = ClientService.getAllClients();
            return emailResponseService.formatListClientsResponse(clients, "LISPER");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar personas: " + e.getMessage(), "LISPER");
        
    }



}
}
