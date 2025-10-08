package com.crm.client.repository;

import com.crm.client.entity.Client;
import com.crm.client.entity.ClientFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientFileRepository extends JpaRepository<ClientFile, Long> {
    Optional<ClientFile> findByIdAndClientId(Long fileId, Long clientId);


}
