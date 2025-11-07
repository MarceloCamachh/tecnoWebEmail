package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Service.service_email.ClientPOP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EmailMonitoringService {
    
    @Autowired
    private ClientPOP clientPOP;
    
    /**
     * Método que se ejecuta cada 30 segundos para revisar correos nuevos
     */
    @Scheduled(fixedDelay = 30000) // 30 segundos
    public void monitorEmails() {
        System.out.println("Verificando nuevos correos...");
        executeEmailProcessing();
    }

    /**
     * Método para procesar correos manualmente
     */
    public void processEmailsManually() {
        System.out.println("Procesando correos manualmente...");
        executeEmailProcessing();
    }
    
    /**
     * Método helper que ejecuta el procesamiento de emails
     */
    private void executeEmailProcessing() {
        try {
            clientPOP.processEmails();
        } catch (Exception e) {
            System.err.println("Error ejecutando procesamiento de emails: " + e.getMessage());
            e.printStackTrace();
        }
    }
}