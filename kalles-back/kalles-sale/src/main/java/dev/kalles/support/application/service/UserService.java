package dev.kalles.support.application.service;

import dev.kalles.support.application.exception.NotFoundException;
import dev.kalles.support.infrastructure.persistence.entity.UserEntity;
import dev.kalles.support.infrastructure.persistence.repository.UserRepository;
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
        return userRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public UserEntity findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Transactional
    public UserEntity create(String email, String name) {
        userRepository.findByEmail(email).ifPresent(existing -> {
            throw new IllegalArgumentException("A user with this email already exists: " + email);
        });
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    /** Finds an existing user by email, or creates one if not found. */
    @Transactional
    public UserEntity findOrCreate(String email, String name) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            UserEntity user = new UserEntity();
            user.setEmail(email);
            user.setName(name);
            return userRepository.save(user);
        });
    }
}
