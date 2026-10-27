package dev.kalles.security.controller;

import dev.kalles.security.context.PosContextHolder;
import dev.kalles.security.dto.PairDeviceRequest;
import dev.kalles.security.dto.PairedDeviceResponse;
import dev.kalles.security.service.PairedDevice;
import dev.kalles.security.service.PosPairingService;
import dev.kalles.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
public class PosSetupController {

    private final PosPairingService pairingService;

    @Value("${app.security.cookies.secure:false}")
    private boolean secureCookies;

    @PostMapping("/setup")
    public ResponseEntity<PairedDeviceResponse> setup(@Valid @RequestBody PairDeviceRequest request) {
        PairedDevice device = pairingService.pair(request.pairingCode());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, PosDeviceCookie.issue(device.deviceToken(), secureCookies).toString())
                .body(new PairedDeviceResponse(device.cashRegisterId(), device.cashRegisterCode()));
    }

    @GetMapping("/device")
    public PairedDeviceResponse device() {
        return Optional.ofNullable(PosContextHolder.getPosId())
                .flatMap(pairingService::describe)
                .orElseThrow(() -> new NotFoundException(
                        "DEVICE_NOT_PAIRED", "Este navegador não está pareado com um caixa."));
    }
}
