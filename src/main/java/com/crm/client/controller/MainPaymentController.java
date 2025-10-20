package com.crm.client.controller;

import com.crm.client.dto.PaymentStatus;
import com.crm.client.entity.Client;
import com.crm.client.service.MainPaymentService;
import com.crm.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/clients/main-payments-advanced")
@RequiredArgsConstructor
public class MainPaymentController {

    private final MainPaymentService mainPaymentService;

    /**
     * ✅ 1. Main payment ma'lumotlarini filter bilan olish
     * Ruxsat: SUPER_ADMIN, ADMIN, MANAGER, FINANCE yoki CLIENT_VIEW, PAYMENT_VIEW
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE') or hasAnyAuthority('CLIENT_VIEW','PAYMENT_VIEW')")
    public ResponseEntity<ApiResponse<List<Client>>> getMainPayments(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        List<Client> result = mainPaymentService.filterMainPayments(region, targetCountry, status, start, end);
        return ResponseEntity.ok(ApiResponse.ok("Filtered main payments", result));
    }

    /**
     * ✅ 2. Main paymentlarni Excel faylga eksport qilish
     * Ruxsat: SUPER_ADMIN, ADMIN, MANAGER, FINANCE yoki CLIENT_EXPORT
     */
    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE') or hasAuthority('CLIENT_EXPORT')")
    public ResponseEntity<InputStreamResource> exportMainPaymentsExcel(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) throws IOException {
        ByteArrayInputStream in = mainPaymentService.exportMainPaymentsToExcel(region, targetCountry, status, start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=main_payments.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    /**
     * ✅ 3. Main paymentlarni CSV faylga eksport qilish
     * Ruxsat: SUPER_ADMIN, ADMIN, MANAGER, FINANCE yoki CLIENT_EXPORT
     */
    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER','FINANCE') or hasAuthority('CLIENT_EXPORT')")
    public ResponseEntity<InputStreamResource> exportMainPaymentsCsv(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String targetCountry,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) throws IOException {
        ByteArrayInputStream in = mainPaymentService.exportMainPaymentsToCsv(region, targetCountry, status, start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=main_payments.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }
}
