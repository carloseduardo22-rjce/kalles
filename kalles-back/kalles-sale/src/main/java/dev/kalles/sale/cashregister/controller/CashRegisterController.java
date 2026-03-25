package dev.kalles.sale.cashregister.controller;

import dev.kalles.sale.cashregister.dto.CashRegisterStatusResponse;
import dev.kalles.sale.cashregister.dto.CreateCashRegisterRequest;
import dev.kalles.sale.cashregister.dto.OperatorResponse;
import dev.kalles.sale.cashregister.service.CashRegisterQueryService;
import dev.kalles.sale.cashregister.service.CashRegisterCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/cash-registers")
@RequiredArgsConstructor
@Tag(name = "Caixas", description = "Gerenciamento de caixas e operadores — visão do ADMIN")
public class CashRegisterController {

    private final CashRegisterQueryService queryService;
    private final CashRegisterCommandService commandService;

    @PostMapping
    @Operation(
        summary = "Criar novo caixa",
        description = "Cadastra um novo caixa registrador com código e descrição."
    )
    public ResponseEntity<Void> create(@RequestBody @Valid CreateCashRegisterRequest request) {
        commandService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(
        summary = "Listar caixas com status da sessão",
        description = "Retorna todos os caixas ativos indicando se há sessão aberta e, caso haja, os dados do operador vinculado."
    )
    public ResponseEntity<List<CashRegisterStatusResponse>> listCashRegisters() {
        return ResponseEntity.ok(queryService.listAllWithSessionStatus());
    }

    @GetMapping("/operators")
    @Operation(
        summary = "Listar operadores disponíveis",
        description = "Retorna todos os operadores cadastrados para seleção ao abrir uma sessão de caixa."
    )
    public ResponseEntity<List<OperatorResponse>> listOperators() {
        return ResponseEntity.ok(queryService.listOperators());
    }
}
