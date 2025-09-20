package com.crm.lead.service;
import com.crm.lead.entity.Lead;
import com.crm.lead.reposiroty.LeadRepository;
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
public class LeadFilterService {

    private final LeadRepository leadRepository;

    // ✅ Filter qilish
    public List<Lead> filterLeads(Long statusId, String region, String targetCountry, LocalDate start, LocalDate end) {
        List<Lead> leads = leadRepository.findAll();

        return leads.stream()
                .filter(l -> statusId == null || (l.getStatus() != null && l.getStatus().getId().equals(statusId)))
                .filter(l -> region == null || region.equalsIgnoreCase(l.getRegion()))
                .filter(l -> targetCountry == null || targetCountry.equalsIgnoreCase(l.getTargetCountry()))
                .filter(l -> start == null || (l.getLastContactDate() != null && !l.getLastContactDate().isBefore(start)))
                .filter(l -> end == null || (l.getLastContactDate() != null && !l.getLastContactDate().isAfter(end)))
                .collect(Collectors.toList());
    }

    // ✅ Batch delete (filterlanganlarni o‘chirish)
    public void deleteFilteredLeads(Long statusId, String region, String targetCountry, LocalDate start, LocalDate end) {
        List<Lead> filtered = filterLeads(statusId, region, targetCountry, start, end);
        leadRepository.deleteAll(filtered);
    }

    // ✅ Excel export
    public ByteArrayInputStream exportLeadsToExcel(Long statusId, String region, String targetCountry, LocalDate start, LocalDate end) throws IOException {
        List<Lead> leads = filterLeads(statusId, region, targetCountry, start, end);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Filtered Leads");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Full Name");
        header.createCell(1).setCellValue("Phone");
        header.createCell(2).setCellValue("Region");
        header.createCell(3).setCellValue("Target Country");
        header.createCell(4).setCellValue("Last Contact Date");

        int rowIdx = 1;
        for (Lead lead : leads) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(lead.getFullName() != null ? lead.getFullName() : "");
            row.createCell(1).setCellValue(lead.getPhone() != null ? lead.getPhone() : "");
            row.createCell(2).setCellValue(lead.getRegion() != null ? lead.getRegion() : "");
            row.createCell(3).setCellValue(lead.getTargetCountry() != null ? lead.getTargetCountry() : "");
            row.createCell(4).setCellValue(lead.getLastContactDate() != null ? lead.getLastContactDate().toString() : "");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return new ByteArrayInputStream(bos.toByteArray());
    }

    // ✅ CSV export
    public ByteArrayInputStream exportLeadsToCsv(Long statusId, String region, String targetCountry, LocalDate start, LocalDate end) throws IOException {
        List<Lead> leads = filterLeads(statusId, region, targetCountry, start, end);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
            writer.write("Full Name,Phone,Region,Target Country,Last Contact Date\n");
            for (Lead lead : leads) {
                writer.write(String.format("%s,%s,%s,%s,%s\n",
                        lead.getFullName() != null ? lead.getFullName() : "",
                        lead.getPhone() != null ? lead.getPhone() : "",
                        lead.getRegion() != null ? lead.getRegion() : "",
                        lead.getTargetCountry() != null ? lead.getTargetCountry() : "",
                        lead.getLastContactDate() != null ? lead.getLastContactDate().toString() : ""
                ));
            }
        }
        return new ByteArrayInputStream(out.toByteArray());
    }


}
