package com.crm.client.controller;

import com.crm.client.dto.ClientRequest;
import com.crm.client.dto.ClientResponse;
import com.crm.client.dto.PaymentStatus;
import com.crm.client.service.ClientService;
import com.crm.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER_CONSULTANT')")
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Client created", clientService.createClient(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE')")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getAllClients() {
        return ResponseEntity.ok(ApiResponse.ok("All clients", clientService.getAllClients()));
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

    // ✅ File upload
    @PostMapping("/{id}/files")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER_CONSULTANT')")
    public ResponseEntity<ApiResponse<ClientResponse>> uploadFile(@PathVariable Long id,
                                                                  @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok("File uploaded", clientService.uploadFile(id, file)));
    }

    // ✅ File download
    @GetMapping("/{id}/files/{fileId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE')")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id, @PathVariable Long fileId) throws IOException {
        byte[] fileData = clientService.downloadFile(id, fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=file_" + fileId)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileData);
    }

    // ✅ File delete
    @DeleteMapping("/{id}/files/{fileId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> deleteFile(@PathVariable Long id, @PathVariable Long fileId) {
        return ResponseEntity.ok(ApiResponse.ok("File deleted", clientService.deleteFile(id, fileId)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientResponse>> searchClients(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) PaymentStatus status) {
        return ResponseEntity.ok(clientService.searchClients(query, status));
    }
    // ✅ Soft Delete
    @DeleteMapping("/{id}/soft")
    public ResponseEntity<Void> softDeleteClient(@PathVariable Long id) {
        clientService.softDeleteClient(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Archive
    @PutMapping("/{id}/archive")
    public ResponseEntity<Void> archiveClient(@PathVariable Long id) {
        clientService.archiveClient(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Restore
    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreClient(@PathVariable Long id) {
        clientService.restoreClient(id);
        return ResponseEntity.noContent().build();
    }

    // ❌ Permanent delete (faqat SUPER_ADMIN ruxsat)
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDeleteClient(@PathVariable Long id) {
        clientService.permanentDeleteClient(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Archived list
    @GetMapping("/archived")
    public ResponseEntity<List<ClientResponse>> getArchivedClients() {
        return ResponseEntity.ok(clientService.getArchivedClients());
    }

    // ✅ Deleted list
    @GetMapping("/deleted")
    public ResponseEntity<List<ClientResponse>> getDeletedClients() {
        return ResponseEntity.ok(clientService.getDeletedClients());
    }
}
