package dev.kalles.support.exception;

import dev.kalles.shared.exception.ConflictException;

public class CategoryAlreadyExistsException extends ConflictException {

    public CategoryAlreadyExistsException(String name, String subcategory) {
        super("SUPPORT_CATEGORY_ALREADY_EXISTS",
                "A category with name '" + name + "' and subcategory '" + subcategory + "' already exists.");
    }
}
