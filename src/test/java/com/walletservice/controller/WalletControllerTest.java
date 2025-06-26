package com.walletservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletservice.domain.Wallet;
import com.walletservice.dto.CreateWalletRequest;
import com.walletservice.dto.DepositRequest;
import com.walletservice.dto.TransferRequest;
import com.walletservice.dto.WithdrawalRequest;
import com.walletservice.repository.WalletRepository;
import com.walletservice.security.util.TestJwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.walletservice.config.TestSecurityConfig;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
    }

    @Test
    void createWallet_Success() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest("user1");

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createWallet_FailsWhenAlreadyExists() throws Exception {
        // Primeiro criamos uma carteira
        Wallet wallet = new Wallet(null, "user1", BigDecimal.ZERO, "BRL");
        walletRepository.save(wallet);

        // Tentamos criar outra com o mesmo ID de usuário
        CreateWalletRequest request = new CreateWalletRequest("user1");

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deposit_Success() throws Exception {
        // Primeiro criamos uma carteira
        Wallet wallet = new Wallet(null, "user1", new BigDecimal("50"), "BRL");
        walletRepository.save(wallet);

        // Fazemos um depósito
        DepositRequest request = new DepositRequest("user1", new BigDecimal("100"));

        mockMvc.perform(post("/api/wallets/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo", is(150)));
    }

    @Test
    void withdraw_Success() throws Exception {
        // Primeiro criamos uma carteira com saldo
        Wallet wallet = new Wallet(null, "user1", new BigDecimal("100"), "BRL");
        walletRepository.save(wallet);

        // Fazemos um saque
        WithdrawalRequest request = new WithdrawalRequest("user1", new BigDecimal("50"));

        mockMvc.perform(post("/api/wallets/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo", is(50)));
    }

    @Test
    void withdraw_InsufficientFunds() throws Exception {
        // Primeiro criamos uma carteira com saldo insuficiente
        Wallet wallet = new Wallet(null, "user1", new BigDecimal("30"), "BRL");
        walletRepository.save(wallet);

        // Tentamos sacar mais do que o saldo disponível
        WithdrawalRequest request = new WithdrawalRequest("user1", new BigDecimal("50"));

        mockMvc.perform(post("/api/wallets/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_Success() throws Exception {
        // Criamos duas carteiras
        Wallet sourceWallet = new Wallet(null, "user1", new BigDecimal("100"), "BRL");
        Wallet targetWallet = new Wallet(null, "user2", new BigDecimal("50"), "BRL");
        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        // Fazemos uma transferência
        TransferRequest request = new TransferRequest("user1", "user2", new BigDecimal("30"));

        mockMvc.perform(post("/api/wallets/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verificamos o saldo da carteira de origem
        mockMvc.perform(get("/api/wallets/{idUsuario}/balance", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo", is(70)));

        // Verificamos o saldo da carteira de destino
        mockMvc.perform(get("/api/wallets/{idUsuario}/balance", "user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo", is(80)));
    }

    @Test
    void getBalance_Success() throws Exception {
        Wallet wallet = new Wallet(null, "user1", new BigDecimal("123.45"), "BRL");
        walletRepository.save(wallet);

        mockMvc.perform(get("/api/wallets/{idUsuario}/balance", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario", is("user1")))
                .andExpect(jsonPath("$.saldo", is(123.45)));
    }

    @Test
    void getBalance_NotFound() throws Exception {
        mockMvc.perform(get("/api/wallets/{idUsuario}/balance", "non-existent-user"))
                .andExpect(status().isNotFound());
    }
}
