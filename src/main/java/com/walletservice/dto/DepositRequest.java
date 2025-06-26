package com.walletservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "Requisição para depósito de fundos em uma carteira")
public record DepositRequest(
    @Schema(description = "ID do usuário", example = "user123")
    @NotBlank(message = "User ID cannot be blank")
    String idUsuario,

    @Schema(description = "Valor a ser depositado", example = "100.00")
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    BigDecimal valor
) {}
