package com.example.tecnoWebEmail.Service.service_email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.tecnoWebEmail.Service.CommandProcessor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ClientPOP {
    private String HOST = ""; 
    private final int PORT = 110;
    private String USER = "";  
    private String PASSWORD = ""; 

    private Socket connection;
    private BufferedReader input;
    private DataOutputStream output;
    
    private CommandProcessor commandProcessor;
    private ClientSMTP smtpClient;

    @Autowired
    public ClientPOP(CommandProcessor commandProcessor) {
        this.HOST = "mail.tecnoweb.org.bo";
        this.USER = "grupo21sa";
        this.PASSWORD = "grup021grup021*";
        this.commandProcessor = commandProcessor;
        this.smtpClient = new ClientSMTP();
    }

    public void connect() throws Exception {
        connection = new Socket(HOST, PORT);
        input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        output = new DataOutputStream(connection.getOutputStream());
        System.out.println("S: " + input.readLine());
    }

    public void close() throws IOException {
        if (this.connection != null) this.connection.close();
        if (this.input != null) this.input.close();
        if (this.output != null) this.output.close();
    }

    public void login() throws IOException {
        sendCommand("USER " + USER + "\r\n");
        sendCommand("PASS " + PASSWORD + "\r\n");
        System.out.println("Login exitoso");
    }

    public void checkAndProcessEmails() throws IOException {
        // Obtener número de mensajes
        String statResponse = sendCommand("STAT\r\n");
        int messageCount = extractMessageCount(statResponse);
        
        System.out.println("Número de mensajes: " + messageCount);
        
        // Procesar cada mensaje
        for (int i = 1; i <= messageCount; i++) {
            processMessage(i);
        }
    }

    private void processMessage(int messageNumber) throws IOException {
        // Obtener el mensaje completo
        String emailContent = sendCommand("RETR " + messageNumber + "\r\n");
        
        // Extraer información del correo
        EmailInfo emailInfo = parseEmail(emailContent);
        
        if (emailInfo != null && emailInfo.subject != null && !emailInfo.subject.trim().isEmpty()) {
            System.out.println("Procesando comando: " + emailInfo.subject);
            
            // Procesar el comando
            System.out.println("DEBUG: Llamando al CommandProcessor...");
            String response;
            try {
                response = commandProcessor.processCommand(emailInfo.subject, emailInfo.from);
                System.out.println("DEBUG: Respuesta del CommandProcessor recibida: " + (response != null ? response.substring(0, Math.min(100, response.length())) + "..." : "null"));
            } catch (Exception e) {
                System.out.println("DEBUG: ERROR en CommandProcessor: " + e.getMessage());
                e.printStackTrace();
                response = "Error interno al procesar comando";
            }
            
            // Enviar respuesta por SMTP
            System.out.println("DEBUG: Iniciando envío de respuesta por SMTP...");
            System.out.println("DEBUG: Host SMTP = " + smtpClient.getServer() + ", Port = " + smtpClient.getPort());
            smtpClient.sendEmail(emailInfo.from, "Re: " + emailInfo.subject, response);
            
            System.out.println("Respuesta enviada a: " + emailInfo.from);
        }
        
        // Marcar mensaje para eliminación después de procesarlo
        sendCommand("DELE " + messageNumber + "\r\n");
    }

    private EmailInfo parseEmail(String emailContent) {
        EmailInfo info = new EmailInfo();
        
        // Extraer From
        Pattern fromPattern = Pattern.compile("From: (.+)", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(emailContent);
        if (fromMatcher.find()) {
            info.from = extractEmailAddress(fromMatcher.group(1));
        }
        
        // Extraer Subject
        Pattern subjectPattern = Pattern.compile("Subject: (.+)", Pattern.CASE_INSENSITIVE);
        Matcher subjectMatcher = subjectPattern.matcher(emailContent);
        if (subjectMatcher.find()) {
            info.subject = subjectMatcher.group(1).trim();
        }
        
        return info;
    }

    private String extractEmailAddress(String fromField) {
        // Extraer email de campos como "Name <email@domain.com>" o "email@domain.com"
        Pattern emailPattern = Pattern.compile("<(.+?)>|([\\w.-]+@[\\w.-]+\\.[a-zA-Z]+)");
        Matcher matcher = emailPattern.matcher(fromField);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        return fromField.trim();
    }

    private int extractMessageCount(String statResponse) {
        Pattern pattern = Pattern.compile("\\+OK (\\d+)");
        Matcher matcher = pattern.matcher(statResponse);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private String sendCommand(String command) throws IOException {
        this.output.writeBytes(command);
        
        if (command.startsWith("RETR") || command.startsWith("LIST")) {
            return readMultilineResponse(input);
        }
        
        return this.input.readLine();
    }

    static protected String readMultilineResponse(BufferedReader text) throws IOException {
        StringBuilder lines = new StringBuilder();
        while (true) {
            String line = text.readLine();
            if (line == null) 
                throw new IOException("S: Server unawares closed the connection");
            if (line.equals(".")) 
                break;
            if (line.startsWith("."))
                line = line.substring(1);
            lines.append("\n").append(line);
        }
        return lines.toString();
    }

    // Clase interna para información del email
    private static class EmailInfo {
        String from;
        String subject;
    }

    // Método para ejecutar el procesamiento completo
    public void processEmails() {
        try {
            connect();
            login();
            checkAndProcessEmails();
            sendCommand("QUIT\r\n");
        } catch (Exception e) {
            System.err.println("Error procesando emails: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                close();
            } catch (IOException e) {
                System.err.println("Error cerrando conexión: " + e.getMessage());
            }
        }
    }
}

