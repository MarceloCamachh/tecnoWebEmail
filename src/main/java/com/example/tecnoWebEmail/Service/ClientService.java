package com.example.tecnoWebEmail.Service;

import com.example.tecnoWebEmail.Models.Client;
import com.example.tecnoWebEmail.Repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    /**
     * Obtiene todos los clientes.
     */
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Optional<Client> getClientByCi(String ci) {
        return clientRepository.findByCi(ci);
    }
    /**
     * Obtiene un cliente por su ID.
     */
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    /**
     * Obtiene un cliente por su email.
     */
    public Optional<Client> getClientByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    /**
     * Crea un nuevo cliente.
     * Valida que el email y el teléfono no estén ya en uso.
     */
    @Transactional
    public Client createClient(Client client) {
        // Validar si el email ya existe
        if (client.getEmail() != null && clientRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use: " + client.getEmail());
        }

        // Validar si el teléfono ya existe
        if (client.getPhone() != null && clientRepository.findByPhone(client.getPhone()).isPresent()) {
            throw new RuntimeException("Phone number already in use: " + client.getPhone());
        }

        return clientRepository.save(client);
    }

    /**
     * Actualiza un cliente existente.
     */
    @Transactional
    public Client updateClient(Long id, Client clientDetails) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));

        // Validar email si ha cambiado
        if (clientDetails.getEmail() != null && !clientDetails.getEmail().equals(client.getEmail())) {
            if (clientRepository.findByEmail(clientDetails.getEmail()).isPresent()) {
                throw new RuntimeException("Email already in use: " + clientDetails.getEmail());
            }
            client.setEmail(clientDetails.getEmail());
        }

        // Validar teléfono si ha cambiado
        if (clientDetails.getPhone() != null && !clientDetails.getPhone().equals(client.getPhone())) {
            if (clientRepository.findByPhone(clientDetails.getPhone()).isPresent()) {
                throw new RuntimeException("Phone number already in use: " + clientDetails.getPhone());
            }
            client.setPhone(clientDetails.getPhone());
        }

        client.setFirstName(clientDetails.getFirstName());
        client.setLastName(clientDetails.getLastName());
        client.setAddress(clientDetails.getAddress());

        return clientRepository.save(client);
    }

}