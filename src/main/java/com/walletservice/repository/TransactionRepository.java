package com.walletservice.repository;

import com.walletservice.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByIdUsuarioOrigemAndDataHoraLessThanEqual(String idUsuario, LocalDateTime dataHora);
}
