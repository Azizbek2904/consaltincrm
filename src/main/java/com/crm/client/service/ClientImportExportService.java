package com.crm.client.service;

import com.crm.client.entity.Client;
import com.crm.client.entity.ClientFile;
import com.crm.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientImportExportService {

    private final ClientRepository clientRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String BASE_FILE_URL = "http://localhost:8080/files/clients/";
    // ❗ bu keyinchalik real hostga o‘zgartiriladi (nginx, s3 va h.k.)

    // ✅ Import qilish (Excel)
    public List<Client> importClients(MultipartFile file) throws IOException {
        List<Client> clients = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 0 - header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Client client = Client.builder()
                        .fullName(getCellValue(row, 0))
                        .phone1(getCellValue(row, 1))
                        .phone2(getCellValue(row, 2))
                        .region(getCellValue(row, 3))
                        .targetCountry(getCellValue(row, 4))
                        .initialPayment(parseDouble(getCellValue(row, 5)))
                        .initialPaymentDate(parseDate(getCellValue(row, 6)))
                        .totalPayment(parseDouble(getCellValue(row, 7)))
                        .totalPaymentDate(parseDate(getCellValue(row, 8)))
                        .build();

                clients.add(client);
            }
        }
        return clientRepository.saveAll(clients);
    }

    // ✅ Export qilish (Excel)
    public ByteArrayInputStream exportClients(String region, String targetCountry, LocalDate start, LocalDate end) throws IOException {
        List<Client> clients = clientRepository.findAll();

        // filtr qo‘llash
        List<Client> filtered = clients.stream()
                .filter(c -> region == null || region.equalsIgnoreCase(c.getRegion()))
                .filter(c -> targetCountry == null || targetCountry.equalsIgnoreCase(c.getTargetCountry()))
                .filter(c -> start == null || (c.getInitialPaymentDate() != null && !c.getInitialPaymentDate().isBefore(start)))
                .filter(c -> end == null || (c.getInitialPaymentDate() != null && !c.getInitialPaymentDate().isAfter(end)))
                .collect(Collectors.toList());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Clients");

        // header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Full Name");
        header.createCell(1).setCellValue("Phone1");
        header.createCell(2).setCellValue("Phone2");
        header.createCell(3).setCellValue("Region");
        header.createCell(4).setCellValue("Target Country");
        header.createCell(5).setCellValue("Initial Payment");
        header.createCell(6).setCellValue("Initial Payment Date");
        header.createCell(7).setCellValue("Total Payment");
        header.createCell(8).setCellValue("Total Payment Date");
        header.createCell(9).setCellValue("Payment Status");
        header.createCell(10).setCellValue("Files (Links)");

        int rowIdx = 1;
        for (Client client : filtered) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(client.getFullName() != null ? client.getFullName() : "");
            row.createCell(1).setCellValue(client.getPhone1() != null ? client.getPhone1() : "");
            row.createCell(2).setCellValue(client.getPhone2() != null ? client.getPhone2() : "");
            row.createCell(3).setCellValue(client.getRegion() != null ? client.getRegion() : "");
            row.createCell(4).setCellValue(client.getTargetCountry() != null ? client.getTargetCountry() : "");
            row.createCell(5).setCellValue(client.getInitialPayment() != null ? client.getInitialPayment() : 0);
            row.createCell(6).setCellValue(client.getInitialPaymentDate() != null ? client.getInitialPaymentDate().toString() : "");
            row.createCell(7).setCellValue(client.getTotalPayment() != null ? client.getTotalPayment() : 0);
            row.createCell(8).setCellValue(client.getTotalPaymentDate() != null ? client.getTotalPaymentDate().toString() : "");
            row.createCell(9).setCellValue(client.getPaymentStatus() != null ? client.getPaymentStatus().name() : "");

            // fayllar linkini qo‘shish
            String fileLinks = client.getFiles() != null
                    ? client.getFiles().stream()
                    .map(ClientFile::getFileName)
                    .map(f -> BASE_FILE_URL + client.getId() + "/" + f)
                    .collect(Collectors.joining(", "))
                    : "";
            row.createCell(10).setCellValue(fileLinks);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new ByteArrayInputStream(bos.toByteArray());
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> null;
        };
    }

    private LocalDate parseDate(String value) {
        try {
            return value != null ? LocalDate.parse(value, formatter) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        try {
            return value != null ? Double.parseDouble(value) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
