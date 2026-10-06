package dev.kalles.product.exception;

import dev.kalles.shared.exception.ConflictException;

public class ProductInternalCodeAlreadyExistsException extends ConflictException {

    public ProductInternalCodeAlreadyExistsException() {
        super("PRODUCT_INTERNAL_CODE_ALREADY_EXISTS", "Ja existe um produto com o codigo interno informado.");
    }
}
