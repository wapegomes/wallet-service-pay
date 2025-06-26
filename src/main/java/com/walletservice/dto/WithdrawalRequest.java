package com.walletservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "Requisição para saque de fundos de uma carteira")
public record WithdrawalRequest(
    @Schema(description = "ID do usuário", example = "user123")
    @NotBlank(message = "User ID cannot be blank")
    String idUsuario,

    @Schema(description = "Valor a ser sacado", example = "50.00")
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    BigDecimal valor
) {}
