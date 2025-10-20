package com.crm.client.repository;

import com.crm.client.dto.PaymentStatus;
import com.crm.client.entity.Client;
import com.crm.client.entity.ClientPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ClientPaymentHistoryRepository extends JpaRepository<ClientPaymentHistory, Long> {
    List<ClientPaymentHistory> findAll();


}
