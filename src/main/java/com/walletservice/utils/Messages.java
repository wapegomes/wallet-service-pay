package com.walletservice.utils;

/**
 * Mensagens utilizadas em toda a aplicação.
 * Centralizar as mensagens facilita a manutenção e possíveis traduções futuras.
 */
public class Messages {
    // Mensagens de erro - usadas principalmente nas exceções
    public static final String WALLET_ALREADY_EXISTS = "Já existe uma carteira para o usuário %s";
    public static final String WALLET_NOT_FOUND = "Não foi encontrada carteira para o usuário %s";
    public static final String INVALID_AMOUNT = "O valor precisa ser maior que zero";
    public static final String INSUFFICIENT_BALANCE = "Saldo insuficiente para completar a operação";
    public static final String INVALID_CURRENCY = "Moeda não suportada no momento";

    // Mensagens de sucesso - usadas nos logs e descrições de transações
    // TODO: No futuro, adicionar mais detalhes como valores e timestamps
    public static final String DEPOSIT_SUCCESS = "Depósito efetuado com sucesso";
    public static final String WITHDRAW_SUCCESS = "Saque efetuado com sucesso";
    public static final String TRANSFER_SUCCESS = "Transferência concluída com sucesso";
    public static final String WALLET_CREATED = "Nova carteira criada com sucesso";

    // Tipos de transação - usados para categorizar as operações no banco
    public static final String TRANSACTION_TYPE_DEPOSIT = "DEPOSITO";
    public static final String TRANSACTION_TYPE_WITHDRAW = "SAQUE";
    public static final String TRANSACTION_TYPE_TRANSFER = "TRANSFERENCIA";

    // Não permitir instanciação desta classe
    private Messages() {
        throw new IllegalStateException("Classe utilitária");
    }
}
