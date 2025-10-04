package com.crm.client.service;

import com.crm.client.dto.*;
import com.crm.client.entity.Client;
import com.crm.client.entity.ClientFile;
import com.crm.client.entity.ClientPaymentHistory;
import com.crm.client.entity.DocumentType;
import com.crm.client.repository.ClientFileRepository;
import com.crm.client.repository.ClientPaymentHistoryRepository;
import com.crm.client.repository.ClientRepository;
import com.crm.common.exception.CustomException;
import com.crm.lead.entity.Lead;
import com.crm.lead.reposiroty.LeadRepository;
import com.crm.reception.entity.VisitSchedule;
import com.crm.reception.entity.VisitStatus;
import com.crm.reception.repository.VisitScheduleRepository;
import com.crm.user.entity.User;
import com.crm.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientFileRepository fileRepository;
    private final ClientPaymentHistoryRepository paymentHistoryRepository;
    private final LeadRepository leadRepository;
    private final VisitScheduleRepository visitScheduleRepository;
    private final UserRepository userRepository;
    private final String UPLOAD_DIR = "uploads/clients/";

    // CREATE
    public ClientResponse createClient(ClientRequest request) {
        Lead lead = request.getLeadId() != null
                ? leadRepository.findById(request.getLeadId()).orElse(null)
                : null;

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
                .paymentStatus(request.getPaymentStatus())
                .contractNumber(request.getContractNumber()) // ✅ qo‘shildi
                .lead(lead)
                .convertedBy(getCurrentUser())
                .deleted(false)
                .archived(false)
                .build();

        return mapToResponse(clientRepository.save(client));
    }
    @Transactional
    public ClientResponse updatePaymentStatus(Long id, PaymentStatus status) {
        System.out.println(">>> UPDATE STATUS kelgan: " + status);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setPaymentStatus(status);
        clientRepository.save(client);

        return mapToResponse(client);
    }



    // ✅ Komment qo‘shish
    public ClientResponse addComment(Long clientId, String comment) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        client.getComments().add(comment);
        clientRepository.save(client);

        return mapToResponse(client);
    }

    // ✅ ConvertedBy set qilish
    public ClientResponse setConvertedBy(Long clientId, Long userId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        client.setConvertedBy(user);
        clientRepository.save(client);

        return mapToResponse(client);
    }



    //get all cliknes
    public List<ClientResponse> getClientsByStatus(String type) {
        List<Client> clients;

        switch (type.toLowerCase()) {
            case "archived":
                clients = clientRepository.findByArchivedTrueAndDeletedFalse();
                break;
            case "deleted":
                clients = clientRepository.findByDeletedTrue();
                break;
            case "active":
            default:
                clients = clientRepository.findByArchivedFalseAndDeletedFalse();
                break;
        }

        return clients.stream()
                .map(this::mapToResponse)
                .toList();
    }



    // File upload
    // ✅ Fayl yuklash
    public ClientResponse uploadFile(Long clientId, MultipartFile file, DocumentType type) throws IOException {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        String clientDir = UPLOAD_DIR + clientId + "/";
        Path uploadPath = Paths.get(clientDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        // Fayl nomini unikallash
        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = clientDir + uniqueName;
        file.transferTo(new File(filePath));

        ClientFile clientFile = ClientFile.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .filePath(filePath)
                .documentType(type)
                .client(client)
                .build();

        fileRepository.save(clientFile);
        return mapToResponse(client);
    }

    // ✅ Fayl o‘chirish
    public void deleteFile(Long clientId, Long fileId) {
        ClientFile file =  fileRepository.findByIdAndClientId(fileId, clientId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));

        File diskFile = new File(file.getFilePath());
        if (diskFile.exists()) diskFile.delete();

        fileRepository.delete(file);
    }
    // ✅ Fayl yangilash (eski faylni o‘chirib, yangi yuklash)
    public ClientResponse updateFile(Long clientId, Long fileId, MultipartFile newFile, DocumentType type) throws IOException {
        ClientFile file =  fileRepository.findByIdAndClientId(fileId, clientId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));

        // Eski faylni diskdan o‘chirish
        File oldFile = new File(file.getFilePath());
        if (oldFile.exists()) oldFile.delete();

        String clientDir = UPLOAD_DIR + clientId + "/";
        Path uploadPath = Paths.get(clientDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String uniqueName = UUID.randomUUID() + "_" + newFile.getOriginalFilename();
        String newFilePath = clientDir + uniqueName;
        newFile.transferTo(new File(newFilePath));

        file.setFileName(newFile.getOriginalFilename());
        file.setFileType(newFile.getContentType());
        file.setFilePath(newFilePath);
        file.setDocumentType(type);

        fileRepository.save(file);
        return mapToResponse(file.getClient());
    }
    // ✅ Fayl preview
    public byte[] previewFile(Long clientId, Long fileId) throws IOException {
        ClientFile file =  fileRepository.findByIdAndClientId(fileId, clientId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));

        Path path = Paths.get(file.getFilePath());
        return Files.readAllBytes(path);
    }

    public String getFileType(Long clientId, Long fileId) {
        ClientFile file =  fileRepository.findByIdAndClientId(fileId, clientId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));
        return file.getFileType() != null ? file.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    // ✅ Fayl nomini olish
    public String getFileName(Long clientId, Long fileId) {
        ClientFile file = fileRepository.findByIdAndClientId(fileId, clientId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));
        return file.getFileName();
    }

    // Add payment
    public ClientResponse addPayment(Long clientId, Double amount, PaymentStatus status) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        ClientPaymentHistory history = ClientPaymentHistory.builder()
                .amount(amount)
                .paymentDate(LocalDateTime.now())
                .status(status)
                .client(client)
                .receivedBy(getCurrentUser())
                .build();

        paymentHistoryRepository.save(history);

        client.setPaymentStatus(status); // oxirgi status
        clientRepository.save(client);

        return mapToResponse(client);
    }

    private ClientResponse mapToResponse(Client client) {
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
                .contractNumber(client.getContractNumber())
                .convertedBy(client.getConvertedBy() != null ? client.getConvertedBy().getFullName() : null)
                .files(client.getFiles() != null
                        ? client.getFiles().stream()
                        .map(f -> ClientFileResponse.builder()
                                .id(f.getId())
                                .fileName(f.getFileName())
                                .fileType(f.getFileType())
                                .documentType(f.getDocumentType().name())
                                .previewUrl("/clients/" + client.getId() + "/files/" + f.getId() + "/preview")
                                .downloadUrl("/clients/" + client.getId() + "/files/" + f.getId() + "/download")
                                .build())
                        .toList()
                        : List.of())
                .payments(client.getPaymentHistory() != null
                        ? client.getPaymentHistory().stream()
                        .map(p -> ClientPaymentHistoryResponse.builder()
                                .id(p.getId())
                                .amount(p.getAmount())
                                .paymentDate(p.getPaymentDate().toLocalDate())
                                .status(String.valueOf(p.getStatus()))
                                .receivedBy(p.getReceivedBy() != null ? p.getReceivedBy().getFullName() : null)
                                .build())
                        .toList()
                        : List.of())
                .nextVisitDate(
                        visitScheduleRepository.findFirstByClientIdAndStatusOrderByScheduledDateTimeAsc(
                                        client.getId(), VisitStatus.PLANNED
                                )
                                .map(VisitSchedule::getScheduledDateTime)
                                .orElse(null)
                )
                .comments(client.getComments())
                .build();
    }


    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User user) {
            return user; // agar entity saqlangan bo‘lsa
        } else if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            // Agar Spring Security UserDetails bo‘lsa
            return userRepository.findByEmail(springUser.getUsername())
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        }

        throw new CustomException("Authenticated user not found", HttpStatus.UNAUTHORIZED);
    }
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ✅ Bitta clientni olish
    public ClientResponse getClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));
        return mapToResponse(client);
    }

    // ✅ PATCH: faqat yuborilgan fieldlarni yangilash
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
        if (request.getContractNumber() != null) client.setContractNumber(request.getContractNumber());

        return mapToResponse(clientRepository.save(client));
    }

    // ✅ Client o‘chirish (hard delete)
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new CustomException("Client not found", HttpStatus.NOT_FOUND);
        }
        clientRepository.deleteById(id);
    }

    // ✅ File yuklash

    // ✅ To‘lov qo‘shish

    // ✅ Soft delete
    public void softDeleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));
        client.setDeleted(true);
        clientRepository.save(client);
    }

    // ✅ Archive
    public void archiveClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));
        client.setArchived(true);
        clientRepository.save(client);
    }

    // ✅ Restore
    public void restoreClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));
        client.setDeleted(false);
        client.setArchived(false);
        clientRepository.save(client);
    }

    // ✅ Permanent delete
    public void permanentDeleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new CustomException("Client not found", HttpStatus.NOT_FOUND);
        }
        clientRepository.deleteById(id);
    }

    // ✅ Authenticated user olish



}
