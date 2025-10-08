package com.crm.client.service;

import com.crm.client.dto.PaymentStatus;
import com.crm.client.entity.Client;
import com.crm.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainPaymentService {

    private final ClientRepository clientRepository;

    // ✅ Filter qilingan main payment clientlarni olish
    public List<Client> filterMainPayments(String region, String targetCountry, PaymentStatus status, LocalDate start, LocalDate end) {
        List<Client> all = clientRepository.findAll();

        return all.stream()
                .filter(Client::isConvertedToMainPayment) // faqat main payment bo‘lgan clientlar
                .filter(c -> region == null || region.isEmpty() || (c.getRegion() != null && c.getRegion().equalsIgnoreCase(region)))
                .filter(c -> targetCountry == null || targetCountry.isEmpty() || (c.getTargetCountry() != null && c.getTargetCountry().equalsIgnoreCase(targetCountry)))
                .filter(c -> status == null || c.getPaymentStatus() == status)
                .filter(c -> start == null || (c.getTotalPaymentDate() != null && !c.getTotalPaymentDate().isBefore(start)))
                .filter(c -> end == null || (c.getTotalPaymentDate() != null && !c.getTotalPaymentDate().isAfter(end)))
                .collect(Collectors.toList());
    }

    // ✅ Excel eksport
    public ByteArrayInputStream exportMainPaymentsToExcel(String region, String targetCountry, PaymentStatus status, LocalDate start, LocalDate end) throws IOException {
        List<Client> clients = filterMainPayments(region, targetCountry, status, start, end);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Main Payments");

        Row header = sheet.createRow(0);
        String[] columns = {"Full Name", "Phone", "Region", "Country", "Total Payment", "Date", "Status"};
        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }

        int rowNum = 1;
        for (Client c : clients) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(c.getFullName() != null ? c.getFullName() : "");
            row.createCell(1).setCellValue(c.getPhone1() != null ? c.getPhone1() : "");
            row.createCell(2).setCellValue(c.getRegion() != null ? c.getRegion() : "");
            row.createCell(3).setCellValue(c.getTargetCountry() != null ? c.getTargetCountry() : "");
            row.createCell(4).setCellValue(c.getTotalPayment() != null ? c.getTotalPayment() : 0);
            row.createCell(5).setCellValue(c.getTotalPaymentDate() != null ? c.getTotalPaymentDate().toString() : "");
            row.createCell(6).setCellValue(c.getPaymentStatus() != null ? c.getPaymentStatus().name() : "PENDING");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ✅ CSV eksport
    public ByteArrayInputStream exportMainPaymentsToCsv(String region, String targetCountry, PaymentStatus status, LocalDate start, LocalDate end) {
        List<Client> clients = filterMainPayments(region, targetCountry, status, start, end);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        writer.println("Full Name,Phone,Region,Country,Total Payment,Date,Status");

        for (Client c : clients) {
            writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                    c.getFullName() != null ? c.getFullName() : "",
                    c.getPhone1() != null ? c.getPhone1() : "",
                    c.getRegion() != null ? c.getRegion() : "",
                    c.getTargetCountry() != null ? c.getTargetCountry() : "",
                    c.getTotalPayment() != null ? c.getTotalPayment() : "",
                    c.getTotalPaymentDate() != null ? c.getTotalPaymentDate() : "",
                    c.getPaymentStatus() != null ? c.getPaymentStatus().name() : "PENDING"
            );
        }

        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
