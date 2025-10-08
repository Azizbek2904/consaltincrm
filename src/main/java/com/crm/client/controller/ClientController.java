package com.crm.client.controller;

import com.crm.client.dto.*;
import com.crm.client.entity.DocumentType;
import com.crm.client.service.ClientService;
import com.crm.common.exception.CustomException;
import com.crm.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    // ✅ 1. Client yaratish
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER_CONSULTANT')")
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Client created", clientService.createClient(request)));
    }

    // ✅ 2. Komment qo‘shish
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER_CONSULTANT')")
    public ResponseEntity<ApiResponse<ClientResponse>> addComment(
            @PathVariable Long id,
            @RequestParam String comment
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Comment added", clientService.addComment(id, comment)));
    }

    // ✅ 3. Converted by
    @PutMapping("/{id}/converted-by/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> setConvertedBy(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("ConvertedBy set", clientService.setConvertedBy(id, userId)));
    }

    @GetMapping("/filters")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getClientsByStatus(
            @RequestParam(defaultValue = "active") String type,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String country) {

        List<ClientResponse> clients = clientService.getClientsByStatus(type, region, country);
        return ResponseEntity.ok(ApiResponse.ok("Clients fetched", clients));
    }

    // ✅ 5. Bitta clientni olish
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE','MANAGER_CONSULTANT')")
    public ResponseEntity<ApiResponse<ClientResponse>> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Client details", clientService.getClient(id)));
    }

    // ✅ 6. Clientni yangilash
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable Long id,
            @RequestBody ClientRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Client updated", clientService.updateClient(id, request)));
    }

    // ✅ Fayl yuklash
    @PostMapping("/{clientId}/files")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','DOCUMENTS')")
    public ResponseEntity<ApiResponse<ClientResponse>> uploadFile(
            @PathVariable Long clientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false, defaultValue = "OTHER") DocumentType type
    ) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(
                "File uploaded successfully",
                clientService.uploadFile(clientId, file, type)
        ));
    }
    @PutMapping("/{id}/convert")
    public ResponseEntity<ApiResponse<String>> convertClient(@PathVariable Long id) {
        clientService.convertToMainPayment(id);
        return ResponseEntity.ok(ApiResponse.ok("✅ Client successfully moved to Main Payment", null));
    }



    // ✅ 2. MainPayment sahifasi uchun barcha convert qilingan clientlarni olish
    @GetMapping("/main-payments")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getMainPayments() {
        List<ClientResponse> list = clientService.getMainPayments();
        ApiResponse<List<ClientResponse>> response = ApiResponse.ok("✅ Main payments fetched successfully", list);
        return ResponseEntity.ok(response);
    }
    // 💬 Add payment comment
    @PostMapping("/{id}/payment-comments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE')")
    public ResponseEntity<ApiResponse<ClientResponse>> addPaymentComment(
            @PathVariable Long id,
            @RequestParam String comment
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "💬 Payment comment added successfully",
                clientService.addPaymentComment(id, comment)
        ));
    }
    @DeleteMapping("/{id}/payment-comments/{index}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE')")
    public ResponseEntity<ApiResponse<ClientResponse>> deletePaymentComment(
            @PathVariable Long id,
            @PathVariable int index
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "🗑️ Payment comment deleted successfully",
                clientService.deletePaymentComment(id, index)
        ));
    }

    // ✏️ Update payment comment
    @PatchMapping("/{id}/payment-comments/{index}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE')")
    public ResponseEntity<ApiResponse<ClientResponse>> updatePaymentComment(
            @PathVariable Long id,
            @PathVariable int index,
            @RequestParam String newComment
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "💬 Payment comment updated successfully",
                clientService.updatePaymentComment(id, index, newComment)
        ));
    }

    // ✅ 3. Total Payment, Sana, Status, Comment’ni yangilash
    @PatchMapping("/{id}/payment")
    public ResponseEntity<ApiResponse<String>> updatePayment(
            @PathVariable Long id,
            @RequestBody UpdatePaymentRequest request
    ) {
        clientService.updatePaymentInfo(id, request);
        ApiResponse<String> response = ApiResponse.ok("💰 Payment info updated successfully", null);
        return ResponseEntity.ok(response);
    }


    // ✅ Fayl yangilash
    @PutMapping("/{clientId}/files/{fileId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','DOCUMENTS')")
    public ResponseEntity<ApiResponse<ClientResponse>> updateFile(
            @PathVariable Long clientId,
            @PathVariable Long fileId,
            @RequestParam("file") MultipartFile newFile,
            @RequestParam(value = "type", required = false, defaultValue = "OTHER") DocumentType type
    ) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(
                "File updated successfully",
                clientService.updateFile(clientId, fileId, newFile, type)
        ));
    }


    // ✅ Fayl o‘chirish
    @DeleteMapping("/{clientId}/files/{fileId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','DOCUMENTS')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long clientId,
            @PathVariable Long fileId
    ) {
        clientService.deleteFile(clientId, fileId);
        return ResponseEntity.ok(ApiResponse.ok("File deleted successfully", null));
    }


    // ✅ Fayl preview
    @GetMapping("/{clientId}/files/{fileId}/preview")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> previewFile(
            @PathVariable Long clientId,
            @PathVariable Long fileId
    ) {
        try {
            byte[] data = clientService.previewFile(clientId, fileId);
            String fileType = clientService.getFileType(clientId, fileId);
            String fileName = clientService.getFileName(clientId, fileId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(fileType))
                    .body(new ByteArrayResource(data));

        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }


    // ✅ Fayl download
    @GetMapping("/{clientId}/files/{fileId}/download")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> downloadFile(
            @PathVariable Long clientId,
            @PathVariable Long fileId
    ) {
        try {
            byte[] data = clientService.downloadFile(clientId, fileId);
            String fileType = clientService.getFileType(clientId, fileId);
            String fileName = clientService.getFileName(clientId, fileId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(fileType))
                    .body(new ByteArrayResource(data));

        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }



    // ✅ 12. Payment qo‘shish
    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE')")
    public ResponseEntity<ApiResponse<ClientResponse>> addPayment(
            @PathVariable Long id,
            @RequestParam Double amount,
            @RequestParam PaymentStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Payment added", clientService.addPayment(id, amount, status)));
    }

    // ✅ 13. Payment status yangilash
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ClientResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody UpdatePaymentStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Payment status updated",
                clientService.updatePaymentStatus(id, request.getPaymentStatus())));
    }

    // ✅ 14. Soft delete
    @DeleteMapping("/{id}/soft")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> softDeleteClient(@PathVariable Long id) {
        clientService.softDeleteClient(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 15. Archive qilish
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> archiveClient(@PathVariable Long id) {
        clientService.archiveClient(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 16. Restore qilish
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> restoreClient(@PathVariable Long id) {
        clientService.restoreClient(id);
        return ResponseEntity.noContent().build();
    }
    // ✅ Komment yangilash
    @PutMapping("/{clientId}/comments/{index}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER_CONSULTANT')")
    public ResponseEntity<ApiResponse<ClientResponse>> updateComment(
            @PathVariable Long clientId,
            @PathVariable int index,
            @RequestParam String newComment
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Comment updated", clientService.updateComment(clientId, index, newComment)));
    }

    // ✅ 17. Permanent delete
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> permanentDeleteClient(@PathVariable Long id) {
        clientService.permanentDeleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
