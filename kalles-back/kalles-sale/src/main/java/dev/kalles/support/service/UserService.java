package dev.kalles.support.service;

import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.support.entity.UserEntity;
import dev.kalles.support.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserEntity> listAll() {
        return userRepository.findAllByTenantIdOrderByNameAsc(currentTenantId());
    }

    @Transactional(readOnly = true)
    public UserEntity findById(UUID id) {
        return userRepository.findByIdAndTenantId(id, currentTenantId())
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public UserEntity findByEmail(String email) {
        return userRepository.findByTenantIdAndEmailIgnoreCase(currentTenantId(), email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Transactional
    public UserEntity create(String email, String name) {
        UUID tenantId = currentTenantId();
        userRepository.findByTenantIdAndEmailIgnoreCase(tenantId, email).ifPresent(existing -> {
            throw new IllegalArgumentException("A user with this email already exists: " + email);
        });
        UserEntity user = new UserEntity();
        user.setTenantId(tenantId);
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    /** Finds an existing user by email, or creates one if not found. */
    @Transactional
    public UserEntity findOrCreate(String email, String name) {
        UUID tenantId = currentTenantId();
        return userRepository.findByTenantIdAndEmailIgnoreCase(tenantId, email).orElseGet(() -> {
            UserEntity user = new UserEntity();
            user.setTenantId(tenantId);
            user.setEmail(email);
            user.setName(name);
            return userRepository.save(user);
        });
    }

    private UUID currentTenantId() {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is required for support users");
        }
        return tenantId;
    }
}
