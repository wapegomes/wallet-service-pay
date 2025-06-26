package com.walletservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para criação de uma nova carteira")
public record CreateWalletRequest(
    @Schema(description = "ID do usuário", example = "user123")
    @NotBlank(message = "User ID cannot be blank")
    String idUsuario
) {}
