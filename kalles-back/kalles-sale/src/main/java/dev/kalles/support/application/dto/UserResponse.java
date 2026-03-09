package dev.kalles.support.application.dto;

import dev.kalles.support.infrastructure.persistence.entity.UserEntity;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String name
) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}
