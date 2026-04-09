package dev.kalles.sale.support;

import org.junit.jupiter.api.Disabled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Disabled("Legacy Mercado Pago tests kept only as rollback/reference while the generic payment context is the active suite.")
public @interface LegacyMercadoPagoReferenceTest {
}
