package com.crm.lead.service;

import com.crm.common.exception.CustomException;
import com.crm.lead.entity.LeadStatus;
import com.crm.lead.reposiroty.LeadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadStatusService {

    private final LeadStatusRepository repository;

    // ✅ Yangi status yaratish
    public LeadStatus createStatus(String name) {
        if (repository.findByNameIgnoreCase(name).isPresent()) {
            throw new CustomException("Status already exists", HttpStatus.BAD_REQUEST);
        }
        LeadStatus status = LeadStatus.builder()
                .name(name.toUpperCase())
                .build();
        return repository.save(status);
    }

    // ✅ Barcha statuslarni olish
    public List<LeadStatus> getAllStatuses() {
        return repository.findAll();
    }

    // ✅ Statusni o‘chirish
    public void deleteStatus(Long id) {
        if (!repository.existsById(id)) {
            throw new CustomException("Status not found", HttpStatus.NOT_FOUND);
        }
        repository.deleteById(id);
    }
}
