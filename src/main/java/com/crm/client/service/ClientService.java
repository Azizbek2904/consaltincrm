package com.crm.client.service;
import com.crm.client.dto.*;
import com.crm.client.entity.*;
import com.crm.client.repository.ClientFileRepository;
import com.crm.client.repository.ClientPaymentHistoryRepository;
import com.crm.client.repository.ClientRepository;
import com.crm.client.repository.ClientStatusRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ClientStatusRepository clientStatusRepository;
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
        if (request.getStatusId() != null) {
            ClientStatus status = clientStatusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new CustomException("Status not found", HttpStatus.NOT_FOUND));
            client.setStatus(status);
        }

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



    public List<ClientResponse> getClientsByStatus(String type, String region, String targetCountry) {
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
                if (region != null && targetCountry != null)
                    clients = clientRepository.findByArchivedFalseAndDeletedFalseAndRegionAndTargetCountry(region, targetCountry);
                else if (region != null)
                    clients = clientRepository.findByArchivedFalseAndDeletedFalseAndRegion(region);
                else if (targetCountry != null)
                    clients = clientRepository.findByArchivedFalseAndDeletedFalseAndTargetCountry(targetCountry);
                else
                    clients = clientRepository.findByArchivedFalseAndDeletedFalse();
                break;
        }

        // 🔹 Static fromEntity o‘rniga, to‘liq mapToResponse ishlatamiz
        return clients.stream()
                .map(this::mapToResponse)
                .toList();
    }




    // ✅ Fayllar bilan ishlash
// =========================

    public ClientResponse uploadFile(Long clientId, MultipartFile file, DocumentType type) throws IOException {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        // 🔹 To‘liq yo‘l (server root bilan)
        String rootPath = System.getProperty("user.dir") + File.separator + "consultingcrm"
                + File.separator + "uploads" + File.separator + "clients";
        Path clientDir = Paths.get(rootPath, String.valueOf(clientId));

        if (!Files.exists(clientDir)) {
            Files.createDirectories(clientDir);
        }

        // 🔹 Fayl nomini unikallash
        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = clientDir.resolve(uniqueName);

        // 🔹 Faylni saqlash
        file.transferTo(filePath.toFile());

        // 🔹 DBga yozish
        ClientFile clientFile = ClientFile.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .filePath(filePath.toString())
                .documentType(type)
                .client(client)
                .build();

        fileRepository.save(clientFile);
        return mapToResponse(client);
    }

    @Transactional
    public void convertToMainPayment(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        // 🔹 Agar allaqachon convert qilingan bo‘lsa, xato qaytaradi
        if (client.isMainPayment()) {
            throw new CustomException("Client already moved to Main Payment", HttpStatus.BAD_REQUEST);
        }

        // ✅ Hamma fieldlar joyida qoladi
        client.setMainPayment(true); // asosiy flag

        // ✅ To‘lov holati yo‘q bo‘lsa default PENDING
        if (client.getPaymentStatus() == null) {
            client.setPaymentStatus(PaymentStatus.PENDING);
        }

        // ✅ TotalPayment yo‘q bo‘lsa boshlang‘ich qiymat beriladi
        if (client.getTotalPayment() == null) {
            client.setTotalPayment(0.0);
        }

        // ✅ TotalPaymentDate mavjud bo‘lmasa bugungi sana
        if (client.getTotalPaymentDate() == null) {
            client.setTotalPaymentDate(LocalDate.now());
        }

        // ✅ Convert qilgan foydalanuvchini belgilash
        client.setConvertedBy(getCurrentUser());

        // ✅ Flag uchun (optional)
        client.setConvertedToMainPayment(true);

        // 🔹 Barcha fayllar, kommentlar, va payment history o‘z holicha saqlanadi
        clientRepository.save(client);
    }


    @Transactional
    public void updatePaymentInfo(Long id, UpdatePaymentRequest req) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new CustomException("Client not found"));

        client.setTotalPayment(req.getTotalPayment());
        client.setTotalPaymentDate(req.getTotalPaymentDate());
        client.setPaymentStatus(req.getPaymentStatus());

        if (req.getComment() != null && !req.getComment().isBlank()) {
            client.getComments().add(req.getComment());
        }

        clientRepository.save(client);
    }

    // ✅ Main Payment sahifasi uchun faqat convert bo‘lgan clientlarni olish
    public List<ClientResponse> getMainPayments() {
        List<Client> clients = clientRepository.findByMainPaymentTrueAndDeletedFalse();

        if (clients.isEmpty()) {
            throw new CustomException("No main payments found", HttpStatus.NOT_FOUND);
        }

        return clients.stream()
                .map(this::mapToResponse)
                .toList();
    }
    public ClientResponse addPaymentComment(Long clientId, String comment) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        // 🧩 Null holatni tekshiramiz
        if (client.getPaymentComments() == null) {
            client.setPaymentComments(new ArrayList<>());
        }

        client.getPaymentComments().add(comment);
        clientRepository.save(client);

        return mapToResponse(client);
    }
    // ✏️ Payment comment tahrirlash (update)
    public ClientResponse updatePaymentComment(Long clientId, int index, String newComment) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        if (!client.isMainPayment()) {
            throw new CustomException("This client is not in main payment list", HttpStatus.BAD_REQUEST);
        }

        List<String> comments = client.getPaymentComments();
        if (comments == null || index < 0 || index >= comments.size()) {
            throw new CustomException("Invalid comment index", HttpStatus.BAD_REQUEST);
        }

        comments.set(index, newComment);
        client.setPaymentComments(comments);
        clientRepository.save(client);

        return mapToResponse(client);
    }


    public ClientResponse deletePaymentComment(Long clientId, int index) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        if (client.getPaymentComments() == null || client.getPaymentComments().isEmpty()) {
            throw new CustomException("No payment comments found", HttpStatus.BAD_REQUEST);
        }

        if (index < 0 || index >= client.getPaymentComments().size()) {
            throw new CustomException("Invalid comment index", HttpStatus.BAD_REQUEST);
        }

        // 🗑️ Commentni o‘chirish
        client.getPaymentComments().remove(index);

        clientRepository.save(client);

        return mapToResponse(client);
    }


    // ✅ Fayl yangilash (eski faylni o‘chirib, yangi yuklash)
    public ClientResponse updateFile(Long clientId, Long fileId, MultipartFile newFile, DocumentType type) throws IOException {
        ClientFile file = fileRepository.findByIdAndClientId(fileId, clientId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));

        // 🔹 Eski faylni o‘chirish
        File oldFile = new File(file.getFilePath());
        if (oldFile.exists()) oldFile.delete();

        // 🔹 Yangi faylni yozish
        String rootPath = System.getProperty("user.dir") + File.separator + "consultingcrm"
                + File.separator + "uploads" + File.separator + "clients";
        Path clientDir = Paths.get(rootPath, String.valueOf(clientId));

        if (!Files.exists(clientDir)) {
            Files.createDirectories(clientDir);
        }

        String uniqueName = UUID.randomUUID() + "_" + newFile.getOriginalFilename();
        Path newFilePath = clientDir.resolve(uniqueName);
        newFile.transferTo(newFilePath.toFile());

        // 🔹 Ma’lumotlarni yangilash
        file.setFileName(newFile.getOriginalFilename());
        file.setFileType(newFile.getContentType());
        file.setFilePath(newFilePath.toString());
        file.setDocumentType(type);

        fileRepository.save(file);
        return mapToResponse(file.getClient());
    }



    // ✅ Fayl o‘chirish
    public void deleteFile(Long clientId, Long fileId) {
        ClientFile file = fileRepository.findByIdAndClientId(fileId, clientId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));

        File diskFile = new File(file.getFilePath());
        if (diskFile.exists()) diskFile.delete();

        fileRepository.delete(file);
    }



    // ✅ Fayl preview (inline ko‘rsatish)
    public byte[] previewFile(Long clientId, Long fileId) throws IOException {
        ClientFile file = fileRepository.findByIdAndClientId(fileId, clientId)
                .orElseThrow(() -> new CustomException("File not found in DB", HttpStatus.NOT_FOUND));

        Path path = Paths.get(file.getFilePath());
        if (!Files.exists(path)) {
            throw new CustomException("File not found on disk", HttpStatus.NOT_FOUND);
        }

        return Files.readAllBytes(path);
    }




    // ✅ Faylni yuklab olish (download uchun)
    public byte[] downloadFile(Long clientId, Long fileId) throws IOException {
        ClientFile file = fileRepository.findByIdAndClientId(fileId, clientId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));

        Path path = Paths.get(file.getFilePath());
        if (!Files.exists(path)) {
            throw new CustomException("File not found on disk", HttpStatus.NOT_FOUND);
        }

        return Files.readAllBytes(path);
    }



    // ✅ Fayl metadata (type va name)
    public String getFileType(Long clientId, Long fileId) {
        ClientFile file = fileRepository.findByIdAndClientId(fileId, clientId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND));
        return file.getFileType() != null ? file.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

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
    // ✅ Komment yangilash
    public ClientResponse updateComment(Long clientId, int index, String newComment) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new CustomException("Client not found", HttpStatus.NOT_FOUND));

        List<String> comments = client.getComments();
        if (index < 0 || index >= comments.size()) {
            throw new CustomException("Invalid comment index", HttpStatus.BAD_REQUEST);
        }

        comments.set(index, newComment);
        client.setComments(comments);
        clientRepository.save(client);

        return mapToResponse(client);
    }



    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            String email = springUser.getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException("User not found in DB: " + email, HttpStatus.NOT_FOUND));
        }

        // 🔹 Agar foydalanuvchi topilmasa — default Super Admin
        return userRepository.findByEmail("atham@gmail.com")
                .orElseThrow(() -> new CustomException("Default admin user not found", HttpStatus.NOT_FOUND));
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
        if (request.getStatusId() != null) {
            ClientStatus status = clientStatusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new CustomException("Status not found", HttpStatus.NOT_FOUND));
            client.setStatus(status);
        }

        return mapToResponse(clientRepository.save(client));
    }

    // ✅ Client o‘chirish (hard delete)
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new CustomException("Client not found", HttpStatus.NOT_FOUND);
        }
        clientRepository.deleteById(id);
    }


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
                .region(client.getRegion())
                .targetCountry(client.getTargetCountry())
                .paymentComments(client.getPaymentComments() != null ? client.getPaymentComments() : new ArrayList<>())
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
                // 🟩🟩🟩 STATUSNI MAP QILISH QISMI — shu joyni qo‘sh
                .statusId(client.getStatus() != null ? client.getStatus().getId() : null)
                .statusName(client.getStatus() != null ? client.getStatus().getName() : null)
                .statusColor(client.getStatus() != null ? client.getStatus().getColor() : null)

                .build();
    }

}
