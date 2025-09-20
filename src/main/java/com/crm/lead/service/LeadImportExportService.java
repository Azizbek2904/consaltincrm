package com.crm.lead.service;

import com.crm.lead.entity.Lead;
import com.crm.lead.reposiroty.LeadRepository;
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
public class LeadImportExportService {

    private final LeadRepository leadRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ✅ Import qilish (Excel)
    public List<Lead> importLeads(MultipartFile file) throws IOException {
        List<Lead> leads = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 0 - header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Lead lead = Lead.builder()
                        .fullName(getCellValue(row, 0))
                        .phone(getCellValue(row, 1))
                        .region(getCellValue(row, 2))
                        .targetCountry(getCellValue(row, 3))
                        .lastContactDate(parseDate(getCellValue(row, 4)))
                        .assignedTo(null) // importda assign qilinmaydi
                        .convertedToClient(false)
                        .build();

                leads.add(lead);
            }
        }
        return leadRepository.saveAll(leads);
    }

    // ✅ Export qilish (Excel)
    public ByteArrayInputStream exportLeads(String region, String targetCountry, LocalDate start, LocalDate end) throws IOException {
        List<Lead> leads = leadRepository.findAll();

        // filtr qo‘llash
        List<Lead> filtered = leads.stream()
                .filter(l -> region == null || region.equalsIgnoreCase(l.getRegion()))
                .filter(l -> targetCountry == null || targetCountry.equalsIgnoreCase(l.getTargetCountry()))
                .filter(l -> start == null || (l.getLastContactDate() != null && !l.getLastContactDate().isBefore(start)))
                .filter(l -> end == null || (l.getLastContactDate() != null && !l.getLastContactDate().isAfter(end)))
                .collect(Collectors.toList());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Leads");

        // header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Full Name");
        header.createCell(1).setCellValue("Phone");
        header.createCell(2).setCellValue("Region");
        header.createCell(3).setCellValue("Target Country");
        header.createCell(4).setCellValue("Last Contact Date");

        int rowIdx = 1;
        for (Lead lead : filtered) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(lead.getFullName() != null ? lead.getFullName() : "");
            row.createCell(1).setCellValue(lead.getPhone() != null ? lead.getPhone() : "");
            row.createCell(2).setCellValue(lead.getRegion() != null ? lead.getRegion() : "");
            row.createCell(3).setCellValue(lead.getTargetCountry() != null ? lead.getTargetCountry() : "");
            row.createCell(4).setCellValue(
                    lead.getLastContactDate() != null ? lead.getLastContactDate().toString() : ""
            );
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
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
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
}
