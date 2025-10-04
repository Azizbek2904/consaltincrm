package com.crm.client.controller;

import com.crm.client.dto.*;
import com.crm.client.entity.DocumentType;
import com.crm.client.repository.ClientFileRepository;
import com.crm.client.repository.ClientPaymentHistoryRepository;
import com.crm.client.service.ClientService;
import com.crm.common.util.ApiResponse;
import com.crm.reception.repository.VisitScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final ClientFileRepository fileRepository;
    private final ClientPaymentHistoryRepository paymentHistoryRepository;
    private final VisitScheduleRepository visitScheduleRepository;

    private final String UPLOAD_DIR = "uploads/clients/";

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER_CONSULTANT')")
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Client created", clientService.createClient(request)));
    }

    // ✅ Komment qo‘shish
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER_CONSULTANT')")
    public ResponseEntity<ApiResponse<ClientResponse>> addComment(
            @PathVariable Long id,
            @RequestParam String comment
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Comment added", clientService.addComment(id, comment)));
    }

    // ✅ ConvertedBy set qilish
    @PutMapping("/{id}/converted-by/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> setConvertedBy(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("ConvertedBy set", clientService.setConvertedBy(id, userId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE')")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getAllClients() {
        return ResponseEntity.ok(ApiResponse.ok("All clients", clientService.getAllClients()));
    }

    @GetMapping("/filters")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE')")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getClientsByStatus(
            @RequestParam(defaultValue = "active") String type
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Filtered clients", clientService.getClientsByStatus(type)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE','MANAGER_CONSULTANT')")
    public ResponseEntity<ApiResponse<ClientResponse>> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Client details", clientService.getClient(id)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(@PathVariable Long id,
                                                                    @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Client updated", clientService.updateClient(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.ok("Client deleted", null));
    }

    // ✅ 1. Fayl yuklash (ALOHIDA URL)
    @PostMapping("/{clientId}/files")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','DOCUMENTS')")
    public ResponseEntity<ApiResponse<ClientResponse>> uploadFile(
            @PathVariable Long clientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false, defaultValue = "OTHER") DocumentType type
    ) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.ok("File uploaded successfully",
                        clientService.uploadFile(clientId, file, type))
        );
    }

    // ✅ 2. Fayl yangilash
    @PutMapping("/{clientId}/files/{fileId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','DOCUMENTS')")
    public ResponseEntity<ApiResponse<ClientResponse>> updateFile(
            @PathVariable Long clientId,
            @PathVariable Long fileId,
            @RequestParam("file") MultipartFile newFile,
            @RequestParam(value = "type", required = false, defaultValue = "OTHER") DocumentType type
    ) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.ok("File updated successfully",
                        clientService.updateFile(clientId, fileId, newFile, type))
        );
    }

    // ✅ 3. Fayl o‘chirish
    @DeleteMapping("/{clientId}/files/{fileId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','DOCUMENTS')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long clientId,
            @PathVariable Long fileId
    ) {
        clientService.deleteFile(clientId, fileId);
        return ResponseEntity.ok(ApiResponse.ok("File deleted successfully", null));
    }

    // ✅ Fayl preview (inline ko‘rish, asl fayl nomi bilan)
    @GetMapping("/{clientId}/files/{fileId}/preview")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','DOCUMENTS','RECEPTION')")
    public ResponseEntity<ByteArrayResource> previewFile(
            @PathVariable Long clientId,
            @PathVariable Long fileId
    ) throws IOException {
        byte[] data = clientService.previewFile(clientId, fileId);
        String fileType = clientService.getFileType(clientId, fileId);
        String fileName = clientService.getFileName(clientId, fileId); // 🔹 asl fayl nomi

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(fileType))
                .body(new ByteArrayResource(data));
    }


    // ✅ Fayl yuklab olish (asl fayl nomi bilan)
    @GetMapping("/{clientId}/files/{fileId}/download")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','DOCUMENTS','RECEPTION')")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @PathVariable Long clientId,
            @PathVariable Long fileId
    ) throws IOException {
        byte[] data = clientService.previewFile(clientId, fileId);
        String fileType = clientService.getFileType(clientId, fileId);
        String fileName = clientService.getFileName(clientId, fileId); // 🔹 asl fayl nomi

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(fileType))
                .body(new ByteArrayResource(data));
    }


    // ✅ Add payment
    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE')")
    public ResponseEntity<ApiResponse<ClientResponse>> addPayment(
            @PathVariable Long id,
            @RequestParam Double amount,
            @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Payment added", clientService.addPayment(id, amount, status)));
    }

    // Soft Delete
    @DeleteMapping("/{id}/soft")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> softDeleteClient(@PathVariable Long id) {
        clientService.softDeleteClient(id);
        return ResponseEntity.noContent().build();
    }

    // Archive
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> archiveClient(@PathVariable Long id) {
        clientService.archiveClient(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ClientResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody UpdatePaymentStatusRequest request
    ) {
        System.out.println(">>> Kelgan request: " + request.getPaymentStatus());
        return ResponseEntity.ok(ApiResponse.ok("Payment status updated",
                clientService.updatePaymentStatus(id, request.getPaymentStatus())));
    }

    // Restore
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> restoreClient(@PathVariable Long id) {
        clientService.restoreClient(id);
        return ResponseEntity.noContent().build();
    }

    // Permanent delete
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> permanentDeleteClient(@PathVariable Long id) {
        clientService.permanentDeleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
