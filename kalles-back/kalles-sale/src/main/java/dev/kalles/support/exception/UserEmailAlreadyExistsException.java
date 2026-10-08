package dev.kalles.support.exception;

import dev.kalles.shared.exception.ConflictException;

public class UserEmailAlreadyExistsException extends ConflictException {

    public UserEmailAlreadyExistsException(String email) {
        super("SUPPORT_USER_EMAIL_ALREADY_EXISTS", "A user with this email already exists: " + email);
    }
}
