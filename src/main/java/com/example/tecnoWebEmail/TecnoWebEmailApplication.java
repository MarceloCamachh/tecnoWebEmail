package com.example.tecnoWebEmail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.tecnoWebEmail.Service.service_email.EmailMonitoringService;

@SpringBootApplication
@EnableScheduling
public class TecnoWebEmailApplication implements CommandLineRunner{
	
	@Autowired
	private EmailMonitoringService emailMonitoringService;
	
	public static void main(String[] args) {
		SpringApplication.run(TecnoWebEmailApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		// Iniciar el monitoreo de correos al arrancar la aplicación
		emailMonitoringService.monitorEmails();
	}
	
}
