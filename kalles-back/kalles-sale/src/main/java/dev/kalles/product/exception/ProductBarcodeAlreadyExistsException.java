package dev.kalles.product.exception;

import dev.kalles.shared.exception.ConflictException;

public class ProductBarcodeAlreadyExistsException extends ConflictException {

    public ProductBarcodeAlreadyExistsException() {
        super("PRODUCT_BARCODE_ALREADY_EXISTS", "Ja existe um produto com o codigo de barras informado.");
    }
}
