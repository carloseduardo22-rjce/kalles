package dev.kalles.sale.core.repository;

import dev.kalles.sale.core.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByCpf(String cpf);

    List<Client> findAllByOrderByNameAsc();
}
