package dev.kalles.client.repository;

import dev.kalles.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByCpf(String cpf);

    Optional<Client> findByCpfAndCompanyId(String cpf, UUID companyId);

    Optional<Client> findByIdAndCompanyId(UUID id, UUID companyId);

    List<Client> findAllByCompanyIdOrderByNameAsc(UUID companyId);

    Page<Client> findAllByCompanyIdOrderByNameAsc(UUID companyId, Pageable pageable);
}
