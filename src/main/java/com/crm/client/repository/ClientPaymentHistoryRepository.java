package com.crm.client.repository;

import com.crm.client.entity.ClientPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientPaymentHistoryRepository extends JpaRepository<ClientPaymentHistory, Long> {
}
