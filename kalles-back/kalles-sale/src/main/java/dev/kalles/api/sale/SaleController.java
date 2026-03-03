package dev.kalles.api.sale;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.kalles.sale.core.dto.AddItemRequest;
import dev.kalles.sale.core.dto.ApplyDiscountRequest;
import dev.kalles.sale.core.dto.PaymentRequest;
import dev.kalles.sale.core.dto.SaleResponse;
import dev.kalles.sale.core.entity.Sale;
import dev.kalles.sale.core.enums.product.ProductCodeType;
import dev.kalles.sale.core.service.PaymentService;
import dev.kalles.sale.core.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Vendas", description = "Operações de venda do PDV")
public class SaleController {

    private final SaleService saleService;
    private final PaymentService paymentService;

    @PostMapping("/{sessionToken}/items")
    @Operation(summary = "Adicionar um item à venda",
            description = "Adiciona um produto à venda ativa da sessão, criando a venda se ainda não existir. Incrementa a quantidade se o produto já estiver na venda.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item adicionado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Sessão de caixa ou produto não encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<SaleResponse> addItem(
            @PathVariable @NotBlank String sessionToken,
            @Valid @RequestBody AddItemRequest request) {

        Sale sale = switch (request.type()) {
            case INTERNAL_CODE -> saleService.addItemByInternalCode(sessionToken, request.code());
            case BAR_CODE -> saleService.addItemByBarCode(sessionToken, request.code());
        };

        return ResponseEntity.ok(SaleResponse.from(sale));
    }

    @DeleteMapping("/{sessionToken}/items/{productCode}")
    @Operation(summary = "Remover um item da venda",
            description = "Remove um produto da venda. Requer que o operador tenha permissão ou forneça um autorizador. O evento de remoção é registrado na auditoria.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
        @ApiResponse(responseCode = "403", description = "Operador sem permissão para remover itens", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Venda, produto ou operador não encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<Void> removeItem(
            @PathVariable @NotBlank String sessionToken,
            @PathVariable @NotBlank String productCode,
            @RequestParam ProductCodeType type,
            @Parameter(name = "X-Operator-Id", description = "ID do operador que solicita a remoção", required = true)
            @NotBlank @RequestHeader("X-Operator-Id") UUID operatorId,
            @Parameter(name = "X-Authorizer-Id", description = "ID do supervisor autorizador (necessário se o operador não tiver permissão própria)", required = false)
            @NotBlank @RequestHeader(value = "X-Authorizer-Id", required = false) UUID authorizerId) {

        if (authorizerId != null) {
            switch (type) {
                case INTERNAL_CODE -> saleService.removeItemByInternalCodeWithAuthorization(
                        sessionToken, productCode, operatorId, authorizerId);
                case BAR_CODE -> saleService.removeItemByBarCodeWithAuthorization(
                        sessionToken, productCode, operatorId, authorizerId);
            }
        } else {
            switch (type) {
                case INTERNAL_CODE -> saleService.removeItemByInternalCode(
                        sessionToken, productCode, operatorId);
                case BAR_CODE -> saleService.removeItemByBarCode(
                        sessionToken, productCode, operatorId);
            }
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{sessionToken}")
    @Operation(summary = "Cancelar uma venda",
            description = "Cancela a venda ativa da sessão. Requer permissão do operador ou autorização de supervisor. O cancelamento é registrado na auditoria.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Venda cancelada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Operador sem permissão para cancelar vendas", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Venda ou operador não encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<Void> cancelSale(
            @PathVariable @NotBlank String sessionToken,
            @Parameter(name = "X-Operator-Id", description = "ID do operador que solicita o cancelamento", required = true)
            @NotBlank @RequestHeader("X-Operator-Id") UUID operatorId,
            @Parameter(name = "X-Authorizer-Id", description = "ID do supervisor autorizador (necessário se o operador não tiver permissão própria)", required = false)
            @NotBlank @RequestHeader(value = "X-Authorizer-Id", required = false) UUID authorizerId) {

        if (authorizerId != null) {
            saleService.cancelSaleWithAuthorization(sessionToken, operatorId, authorizerId);
        } else {
            saleService.cancelSale(sessionToken, operatorId);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionToken}/payments")
    @Operation(summary = "Registrar um pagamento para a venda",
            description = "Processa um pagamento para a venda ativa. Suporta múltiplos métodos (CASH, PIX, CREDIT_CARD, DEBIT_CARD). A venda avança para PAID automaticamente quando o saldo devedor chegar a zero.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagamento registrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Venda ou sessão não encontrada", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "409", description = "Estado da venda não permite pagamento", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<SaleResponse> addPayment(
            @PathVariable @NotBlank String sessionToken,
            @Valid @RequestBody PaymentRequest request) {

        Sale sale = paymentService.addPayment(sessionToken, request.method(), request.amount());
        return ResponseEntity.ok(SaleResponse.from(sale));
    }

    @PostMapping("/{sessionToken}/complete")
    @Operation(summary = "Concluir a venda",
            description = "Finaliza uma venda que já está no estado PAID. Exige que não haja saldo devedor pendente (amountDue = 0).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Venda concluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Nenhuma venda paga encontrada para esta sessão", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "409", description = "Venda com saldo devedor pendente", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<Void> completeSale(
            @PathVariable @NotBlank String sessionToken) {

        saleService.completeSale(sessionToken);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{sessionToken}/items/discount")
    @Operation(summary = "Aplicar desconto em um item da venda",
            description = "Aplica um desconto por valor fixo (não percentual) em um item específico da venda. Disponível apenas no estado OPEN.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Desconto aplicado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Desconto inválido (negativo ou maior que o valor do item)", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "Venda ou item não encontrado", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "409", description = "Estado da venda não permite desconto", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<Void> applyItemDiscount(
            @PathVariable @NotBlank String sessionToken,
            @Valid @RequestBody ApplyDiscountRequest request) {

        saleService.applyItemDiscount(sessionToken, request.itemId(), request.discountAmount());
        return ResponseEntity.noContent().build();
    }
}
