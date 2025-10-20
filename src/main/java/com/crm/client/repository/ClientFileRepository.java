package com.crm.client.repository;
import com.crm.client.entity.ClientFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface ClientFileRepository extends JpaRepository<ClientFile, Long> {
    Optional<ClientFile> findByIdAndClientId(Long fileId, Long clientId);
    @Query("SELECT f FROM ClientFile f JOIN FETCH f.client")
    List<ClientFile> findAllWithClient();
    long count();
}
