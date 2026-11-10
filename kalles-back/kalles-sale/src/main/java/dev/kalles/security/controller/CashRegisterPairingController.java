package dev.kalles.security.controller;

import dev.kalles.security.dto.PairingCodeResponse;
import dev.kalles.security.service.PosPairingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cash-registers/{cashRegisterId}")
@RequiredArgsConstructor
public class CashRegisterPairingController {

    private final PosPairingService pairingService;

    @PostMapping("/pairing-code")
    @ResponseStatus(HttpStatus.CREATED)
    public PairingCodeResponse generatePairingCode(@PathVariable UUID cashRegisterId) {
        return pairingService.generateCode(cashRegisterId);
    }

    @DeleteMapping("/pairing")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokePairing(@PathVariable UUID cashRegisterId) {
        pairingService.revoke(cashRegisterId);
    }
}
