package com.walletservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "Requisição para transferência de fundos entre carteiras")
public record TransferRequest(
    @Schema(description = "ID do usuário de origem", example = "user123")
    @NotBlank(message = "Source User ID cannot be blank")
    String idUsuarioOrigem,

    @Schema(description = "ID do usuário de destino", example = "user456")
    @NotBlank(message = "Destination User ID cannot be blank")
    String idUsuarioDestino,

    @Schema(description = "Valor a ser transferido", example = "75.00")
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    BigDecimal valor
) {}
