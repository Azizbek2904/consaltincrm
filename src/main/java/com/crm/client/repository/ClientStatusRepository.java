package com.crm.client.repository;


import com.crm.client.entity.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientStatusRepository extends JpaRepository<ClientStatus, Long> {
    Optional<ClientStatus> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
