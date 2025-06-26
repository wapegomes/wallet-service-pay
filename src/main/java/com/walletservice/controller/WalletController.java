package com.walletservice.controller;

import com.walletservice.dto.*;
import com.walletservice.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/wallets")
@Tag(name = "Wallet Controller", description = "API para gerenciamento de carteiras digitais")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // Endpoint para criar uma nova carteira
    @PostMapping
    @Operation(summary = "Criar carteira", description = "Cria uma nova carteira para um usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Carteira criada com sucesso"),
        @ApiResponse(responseCode = "409", description = "Carteira já existe",
                     content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        walletService.createWallet(request);
        // Retorna 201 Created sem corpo
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // Endpoint para depositar dinheiro
    @PostMapping("/deposit")
    @Operation(summary = "Depositar", description = "Adiciona fundos à carteira")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Depósito realizado",
                     content = @Content(schema = @Schema(implementation = BalanceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Carteira não encontrada"),
        @ApiResponse(responseCode = "400", description = "Valor inválido")
    })
    public ResponseEntity<BalanceResponse> deposit(@Valid @RequestBody DepositRequest request) {
        var wallet = walletService.deposit(request);
        return ResponseEntity.ok(new BalanceResponse(wallet.getIdUsuario(), wallet.getSaldo(), wallet.getMoeda()));
    }

    // Endpoint para sacar dinheiro
    @PostMapping("/withdraw")
    @Operation(summary = "Sacar", description = "Remove fundos da carteira")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Saque realizado"),
        @ApiResponse(responseCode = "404", description = "Carteira não encontrada"),
        @ApiResponse(responseCode = "400", description = "Valor inválido ou saldo insuficiente")
    })
    public ResponseEntity<BalanceResponse> withdraw(@Valid @RequestBody WithdrawalRequest request) {
        var wallet = walletService.withdraw(request);
        return ResponseEntity.ok(new BalanceResponse(wallet.getIdUsuario(), wallet.getSaldo(), wallet.getMoeda()));
    }

    // Endpoint para transferir entre carteiras
    @PostMapping("/transfer")
    @Operation(summary = "Transferir", description = "Transfere fundos entre carteiras")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transferência realizada"),
        @ApiResponse(responseCode = "404", description = "Carteira não encontrada"),
        @ApiResponse(responseCode = "400", description = "Valor inválido ou saldo insuficiente")
    })
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request) {
        walletService.transfer(request);
        // Não retorna o saldo para não expor informações desnecessárias
        return ResponseEntity.ok().build();
    }

    // Endpoint para consultar saldo atual
    @GetMapping("/{idUsuario}/balance")
    @Operation(summary = "Consultar saldo", description = "Retorna o saldo atual")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Saldo consultado"),
        @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    public ResponseEntity<BalanceResponse> getBalance(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable String idUsuario) {
        return ResponseEntity.ok(walletService.getBalance(idUsuario));
    }

    // Endpoint para consultar saldo histórico
    // Útil para relatórios e conciliação financeira
    @GetMapping("/{idUsuario}/balance/historical")
    @Operation(summary = "Saldo histórico", description = "Retorna o saldo em uma data específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Saldo histórico consultado"),
        @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    public ResponseEntity<BalanceResponse> getHistoricalBalance(
            @PathVariable String idUsuario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        return ResponseEntity.ok(walletService.getHistoricalBalance(idUsuario, dateTime));
    }
}
