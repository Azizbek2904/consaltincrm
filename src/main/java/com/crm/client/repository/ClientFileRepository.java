package com.crm.client.repository;

import com.crm.client.entity.ClientFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientFileRepository extends JpaRepository<ClientFile, Long> {
}
