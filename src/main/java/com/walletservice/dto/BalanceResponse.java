package com.walletservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Resposta contendo informações de saldo da carteira")
public record BalanceResponse(
    @Schema(description = "ID do usuário", example = "user123")
    String idUsuario,

    @Schema(description = "Saldo atual da carteira", example = "150.75")
    BigDecimal saldo,

    @Schema(description = "Moeda do saldo", example = "BRL")
    String moeda
) {}
