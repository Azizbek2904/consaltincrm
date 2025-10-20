package com.crm.client.controller;

import com.crm.client.entity.ClientStatus;
import com.crm.client.service.ClientService;
import com.crm.client.service.ClientStatusService;
import com.crm.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client-statuses")
@RequiredArgsConstructor
public class ClientStatusController {

    private final ClientStatusService clientStatusService;
    private final ClientStatusService clientService;
    // ✅ Barcha statuslarni olish
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClientStatus>>> getAllStatuses() {
        return ResponseEntity.ok(
                ApiResponse.ok("All client statuses", clientStatusService.getAllStatuses())
        );
    }

    // ✅ Yangi status yaratish
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ClientStatus>> createStatus(
            @RequestParam String name,
            @RequestParam(required = false) String color
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Client status created", clientStatusService.createStatus(name, color))
        );
    }



    // ✅ O‘chirish — to‘g‘rilangan
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStatus(@PathVariable Long id) {
        clientStatusService.deleteStatus(id);
        return ResponseEntity.ok(ApiResponse.ok("Status deleted", null)); // ✅ endi to‘g‘ri
    }
}
