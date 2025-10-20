package com.crm.client.service;


import com.crm.client.entity.Client;
import com.crm.client.entity.ClientStatus;
import com.crm.client.repository.ClientRepository;
import com.crm.client.repository.ClientStatusRepository;
import com.crm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientStatusService {

    private final ClientStatusRepository statusRepository;
    private final ClientRepository clientRepository;
    private final ClientStatusRepository clientStatusRepository;

    // ✅ Barcha statuslarni olish
    public List<ClientStatus> getAllStatuses() {
        return statusRepository.findAll();
    }

    // ✅ Status yaratish
    public ClientStatus createStatus(String name, String color) {
        if (statusRepository.existsByNameIgnoreCase(name)) {
            throw new CustomException("Status already exists", HttpStatus.BAD_REQUEST);
        }

        ClientStatus status = ClientStatus.builder()
                .name(name)
                .color(color)
                .build();
        return statusRepository.save(status);
    }
    // ✅ Update client status service method
    public void updateStatus(Long clientId, Long statusId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        ClientStatus status = clientStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found"));

        client.setStatus(status);
        clientRepository.save(client);
    }



    // ✅ Status o‘chirish
    public void deleteStatus(Long id) {
        statusRepository.deleteById(id);
    }
}
