package com.crm.client.service;

import com.crm.client.dto.ClientRequest;
import com.crm.client.dto.ClientResponse;
import com.crm.client.dto.PaymentStatus;
import com.crm.client.entity.Client;
import com.crm.client.entity.ClientFile;
import com.crm.client.repository.ClientFileRepository;
import com.crm.client.repository.ClientRepository;
import com.crm.common.exception.CustomException;
import com.crm.lead.dto.LeadContactHistoryResponse;
import com.crm.lead.service.LeadContactHistoryService;
import com.crm.reception.entity.VisitSchedule;
import com.crm.reception.entity.VisitStatus;
import com.crm.reception.repository.VisitScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {
    @Autowired
    private LeadContactHistoryService historyService;

    private final ClientRepository clientRepository;
    private final ClientFileRepository fileRepository;
    private final VisitScheduleRepository visitScheduleRepository;

    private final String UPLOAD_DIR = "uploads/clients/";

    // ✅ Client yaratish
    public ClientResponse createClient(ClientRequest request) {
        Client client = Client.builder()
                .fullName(request.getFullName())
                .phone1(request.getPhone1())
                .phone2(request.getPhone2())
                .region(request.getRegion())
                .targetCountry(request.getTargetCountry())
                .initialPayment(request.getInitialPayment())
                .initialPaymentDate(request.getInitialPaymentDate())
                .totalPayment(request.getTotalPayment())
                .totalPaymentDate(request.getTotalPaymentDate())
                .paymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : PaymentStatus.PENDING)
                .build();

        clientRepository.save(client);
        return mapToResponse(client);
    }


    // ✅ Barcha clientlarni olish
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ Bitta clientni olish
    public ClientResponse getClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));
        return mapToResponse(client);
    }

    // ✅ PATCH: faqat yuborilgan maydonlarni yangilash
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        if (request.getFullName() != null) client.setFullName(request.getFullName());
        if (request.getPhone1() != null) client.setPhone1(request.getPhone1());
        if (request.getPhone2() != null) client.setPhone2(request.getPhone2());
        if (request.getRegion() != null) client.setRegion(request.getRegion());
        if (request.getTargetCountry() != null) client.setTargetCountry(request.getTargetCountry());
        if (request.getInitialPayment() != null) client.setInitialPayment(request.getInitialPayment());
        if (request.getInitialPaymentDate() != null) client.setInitialPaymentDate(request.getInitialPaymentDate());
        if (request.getTotalPayment() != null) client.setTotalPayment(request.getTotalPayment());
        if (request.getTotalPaymentDate() != null) client.setTotalPaymentDate(request.getTotalPaymentDate());
        if (request.getPaymentStatus() != null) client.setPaymentStatus(request.getPaymentStatus());

        clientRepository.save(client);
        return mapToResponse(client);
    }

    // ✅ Client o‘chirish
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new CustomException("Client not found", HttpStatus.NOT_FOUND);
        }
        clientRepository.deleteById(id);
    }

    // ✅ File yuklash
    public ClientResponse uploadFile(Long clientId, MultipartFile file) throws IOException {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        String clientDir = UPLOAD_DIR + clientId + "/";
        Path uploadPath = Paths.get(clientDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filePath = clientDir + file.getOriginalFilename();
        File dest = new File(filePath);
        file.transferTo(dest);

        ClientFile clientFile = ClientFile.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .filePath(filePath)
                .client(client)
                .build();

        fileRepository.save(clientFile);
        return mapToResponse(client);
    }

    // ✅ File yuklab olish
    public byte[] downloadFile(Long clientId, Long fileId) throws IOException {
        ClientFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));

        if (!file.getClient().getId().equals(clientId)) {
            throw new CustomException("File does not belong to this client", HttpStatus.FORBIDDEN);
        }

        Path path = Paths.get(file.getFilePath());
        return Files.readAllBytes(path);
    }

    // ✅ File o‘chirish
    public ClientResponse deleteFile(Long clientId, Long fileId) {
        ClientFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));

        if (!file.getClient().getId().equals(clientId)) {
            throw new CustomException("File does not belong to this client", HttpStatus.FORBIDDEN);
        }

        File physicalFile = new File(file.getFilePath());
        if (physicalFile.exists()) {
            physicalFile.delete();
        }

        fileRepository.delete(file);
        return mapToResponse(file.getClient());
    }
    public List<ClientResponse> searchClients(String query, PaymentStatus status) {
        List<Client> clients = clientRepository.searchClients(query, status);
        return clients.stream().map(this::mapToResponse).toList();
    }
    public void softDeleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));
        client.setDeleted(true);
        clientRepository.save(client);
    }

    public void archiveClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));
        client.setArchived(true);
        clientRepository.save(client);
    }

    public void restoreClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));
        client.setDeleted(false);
        client.setArchived(false);
        clientRepository.save(client);
    }

    public void permanentDeleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new CustomException("Client not found", HttpStatus.NOT_FOUND);
        }
        clientRepository.deleteById(id);
    }

    public List<ClientResponse> getArchivedClients() {
        return clientRepository.findAllArchived().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ClientResponse> getDeletedClients() {
        return clientRepository.findAllDeleted().stream()
                .map(this::mapToResponse)
                .toList();
    }


    private ClientResponse mapToResponse(Client client) {
        // 📌 LeadContactHistory olish
        List<LeadContactHistoryResponse> history = List.of();
        if (client.getLeadId() != null) { // Agar client lead’dan convert qilingan bo‘lsa
            history = historyService.getLeadHistory(client.getLeadId());
        }

        // 📌 Reception belgilagan keladigan vaqtni olish
        LocalDateTime nextVisit = visitScheduleRepository
                .findFirstByClientIdAndStatusOrderByScheduledDateTimeAsc(
                        client.getId(), VisitStatus.PLANNED
                )
                .map(VisitSchedule::getScheduledDateTime)
                .orElse(null);

        return ClientResponse.builder()
                .id(client.getId())
                .fullName(client.getFullName())
                .phone1(client.getPhone1())
                .phone2(client.getPhone2())
                .region(client.getRegion())
                .targetCountry(client.getTargetCountry())
                .initialPayment(client.getInitialPayment())
                .initialPaymentDate(client.getInitialPaymentDate())
                .totalPayment(client.getTotalPayment())
                .totalPaymentDate(client.getTotalPaymentDate())
                .paymentStatus(client.getPaymentStatus())
                .files(client.getFiles() != null
                        ? client.getFiles().stream().map(ClientFile::getFileName).toList()
                        : List.of())
                .contactHistory(history)   // ✅ Operator gaplashgan tarixi
                .nextVisitDate(nextVisit)  // ✅ Reception belgilagan tashrif
                .build();
    }

}
