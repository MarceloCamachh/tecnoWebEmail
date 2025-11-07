package com.example.tecnoWebEmail.Service.service_email;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientSMTP {
    private String server = ""; 
    private final int port = 25; 
    private String emisor = "";

    public ClientSMTP() {
        this.server = "mail.tecnoweb.org.bo";
        this.emisor = "grupo21sa@tecnoweb.org.bo";
    }

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    private void sendCommand(DataOutputStream output, BufferedReader input, String command) throws IOException {
        output.writeBytes(command);
        String response = readResponse(input);
        int codeResponse = Integer.parseInt(response.substring(0, 3));
        if (codeResponse >= 400) {
            throw new IOException("No se pudo enviar el correo, error durante el comando: " + command + ".\nError: " + response);
        }
        System.out.println("S: " + response.trim());
    }

    static String readResponse(BufferedReader text) throws IOException {
        StringBuilder lines = new StringBuilder();
        String line;
        while ((line = text.readLine()) != null) {
            lines.append(line).append("\n");
            if (line.length() > 3 && line.charAt(3) == ' ') break;
        }
        if (line == null) {
            throw new IOException("S: Server unawares closed the connection.");
        }
        return lines.toString();
    }

    public void sendEmail(String userReceiving, String subject, String message) {
        try (Socket socket = new Socket(server, port);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream salida = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Conectado al servidor SMTP");
            System.out.println("S: " + entrada.readLine());

            sendCommand(salida, entrada, "HELO " + server + "\r\n");
            sendCommand(salida, entrada, "MAIL FROM: <" + emisor + ">\r\n");
            sendCommand(salida, entrada, "RCPT TO: <" + userReceiving + ">\r\n");
            sendCommand(salida, entrada, "DATA\r\n");
            
            // Enviar headers y contenido del mensaje
            String emailContent = "From: " + emisor + "\r\n" +
                                 "To: " + userReceiving + "\r\n" +
                                 "Subject: " + subject + "\r\n" +
                                 "\r\n" +
                                 message.replace("\n", "\r\n") + "\r\n.\r\n";
            
            salida.writeBytes(emailContent);
            String dataResponse = readResponse(entrada);
            System.out.println("S: " + dataResponse.trim());
            
            sendCommand(salida, entrada, "QUIT\r\n");
            
            System.out.println("Correo enviado exitosamente a: " + userReceiving);

        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
