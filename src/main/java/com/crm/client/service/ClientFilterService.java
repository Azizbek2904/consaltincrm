package com.crm.client.service;

import com.crm.client.dto.PaymentStatus;
import com.crm.client.entity.Client;
import com.crm.client.entity.ClientFile;
import com.crm.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientFilterService {

    private final String BASE_FILE_URL = "http://localhost:8080/files/clients/"; // real hostga almashtirasiz
    private final ClientRepository clientRepository;

    // ✅ Filter qilish
    public List<Client> filterClients(PaymentStatus status, String targetCountry, LocalDate start, LocalDate end) {
        List<Client> clients = clientRepository.findAll();

        return clients.stream()
                .filter(c -> status == null || c.getPaymentStatus() == status)
                .filter(c -> targetCountry == null || targetCountry.equalsIgnoreCase(c.getTargetCountry()))
                .filter(c -> start == null || (c.getInitialPaymentDate() != null && !c.getInitialPaymentDate().isBefore(start)))
                .filter(c -> end == null || (c.getInitialPaymentDate() != null && !c.getInitialPaymentDate().isAfter(end)))
                .collect(Collectors.toList());
    }

    // ✅ Batch delete (filterlanganlarni o‘chirish)
    public void deleteFilteredClients(PaymentStatus status, String targetCountry, LocalDate start, LocalDate end) {
        List<Client> filtered = filterClients(status, targetCountry, start, end);
        clientRepository.deleteAll(filtered);
    }


    // ✅ Excel export
    public ByteArrayInputStream exportClientsToExcel(PaymentStatus status, String targetCountry, LocalDate start, LocalDate end) throws IOException {
        List<Client> clients = filterClients(status, targetCountry, start, end);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Filtered Clients");

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
        for (Client client : clients) {
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

            // fayllar linklari
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

    // ✅ CSV export
    public ByteArrayInputStream exportClientsToCsv(PaymentStatus status, String targetCountry, LocalDate start, LocalDate end) throws IOException {
        List<Client> clients = filterClients(status, targetCountry, start, end);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
            writer.write("Full Name,Phone1,Phone2,Region,Target Country,Initial Payment,Initial Payment Date,Total Payment,Total Payment Date,Payment Status,Files\n");
            for (Client client : clients) {
                String fileLinks = client.getFiles() != null
                        ? client.getFiles().stream()
                        .map(ClientFile::getFileName)
                        .map(f -> BASE_FILE_URL + client.getId() + "/" + f)
                        .collect(Collectors.joining(";"))
                        : "";
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        client.getFullName() != null ? client.getFullName() : "",
                        client.getPhone1() != null ? client.getPhone1() : "",
                        client.getPhone2() != null ? client.getPhone2() : "",
                        client.getRegion() != null ? client.getRegion() : "",
                        client.getTargetCountry() != null ? client.getTargetCountry() : "",
                        client.getInitialPayment() != null ? client.getInitialPayment() : "",
                        client.getInitialPaymentDate() != null ? client.getInitialPaymentDate().toString() : "",
                        client.getTotalPayment() != null ? client.getTotalPayment() : "",
                        client.getTotalPaymentDate() != null ? client.getTotalPaymentDate().toString() : "",
                        client.getPaymentStatus() != null ? client.getPaymentStatus().name() : "",
                        fileLinks
                ));
            }
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

}
