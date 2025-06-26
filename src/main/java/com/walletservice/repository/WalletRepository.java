package com.walletservice.repository;

import com.walletservice.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Wallet findByIdUsuario(String idUsuario);
}
