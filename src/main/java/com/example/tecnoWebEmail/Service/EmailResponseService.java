package com.example.tecnoWebEmail.Service;
import org.springframework.stereotype.Service;

import com.example.tecnoWebEmail.Models.Client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailResponseService {
    private static final String SEPARATOR = "================================================";
    private static final String MINI_SEPARATOR = "------------------------";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Genera el encabezado estándar para todas las respuestas
     */
    public String generateHeader(String comando) {
        StringBuilder header = new StringBuilder();
        header.append(SEPARATOR).append("\n");
        header.append("    SISTEMA DE GESTIÓN - GRUPO 21SA\n");
        header.append("    Respuesta para comando: ").append(comando).append("\n");
        header.append("    Fecha: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        header.append(SEPARATOR).append("\n\n");
        return header.toString();
    }
    /**
     * Formatea la respuesta para listar clientss
     */

     public String formatErrorResponse(String error, String command) {
        StringBuilder response = new StringBuilder();
        response.append(generateHeader(command));
        
        response.append(" ERROR EN LA OPERACIÓN\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("Se produjo un error al procesar su solicitud:\n\n");
        response.append(" Detalle del error:\n");
        response.append("   ").append(error).append("\n\n");
        
        response.append(" AYUDA:\n");
        response.append("   • Verifique que todos los parámetros sean correctos\n");
        response.append("   • Asegúrese de usar el formato correcto del comando\n");
        response.append("   • Contacte al administrador si el problema persiste\n");

        return response.toString();
    }
     public String formatUnknownCommandResponse(String command) {
        StringBuilder response = new StringBuilder();
        response.append(generateHeader("COMANDO NO RECONOCIDO"));
        
        response.append(" COMANDO NO VÁLIDO\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("El comando '").append(command).append("' no es reconocido por el sistema.\n\n");
        
        response.append(" COMANDOS DISPONIBLES:\n\n");
        response.append(" GESTIÓN DE PERSONAS:\n");
        response.append("   • LISPER[\"*\"] - Listar todas las personas\n");
        response.append("   • INSPER[\"ci\",\"nombre\",\"apellido\",\"cargo\",\"telefono\",\"celular\",\"email\"]\n");
        response.append("   • UPDPER[\"ci\",\"nombre\",\"apellido\",\"cargo\",\"telefono\",\"celular\",\"email\"]\n");
        response.append("   • DELPER[\"ci\"] - Eliminar persona\n");
        response.append("   • BUSPER[\"ci\"] - Buscar persona por CI\n\n");
        
        response.append(" GESTIÓN DE CLIENTES:\n");
        response.append("   • LISCLI[\"*\"] - Listar todos los clientes\n");
        response.append("   • INSCLI[\"nombre\",\"email\",\"telefono\",\"direccion\"]\n\n");
        
        response.append(" NOTA: Todos los parámetros deben ir entre comillas dobles.\n");

        return response.toString();
    }
}
