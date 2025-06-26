package com.walletservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tipoTransacao;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private String moeda;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private String idUsuarioOrigem;

    @Column(nullable = true)
    private String idUsuarioDestino;

    @Column(nullable = false)
    private String status;

    @Column(nullable = true)
    private String descricao;

    public Transaction() {
        this.valor = BigDecimal.ZERO;
        this.moeda = "BRL";
        this.dataHora = LocalDateTime.now();
        this.status = "COMPLETED";
    }

    public Transaction(UUID id, String tipoTransacao, BigDecimal valor, String moeda,
                      LocalDateTime dataHora, String idUsuarioOrigem, String idUsuarioDestino,
                      String status, String descricao) {
        this.id = id;
        this.tipoTransacao = tipoTransacao;
        this.valor = valor != null ? valor : BigDecimal.ZERO;
        this.moeda = moeda != null ? moeda : "BRL";
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
        this.idUsuarioOrigem = idUsuarioOrigem;
        this.idUsuarioDestino = idUsuarioDestino;
        this.status = status != null ? status : "COMPLETED";
        this.descricao = descricao;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTipoTransacao() {
        return tipoTransacao;
    }

    public void setTipoTransacao(String tipoTransacao) {
        this.tipoTransacao = tipoTransacao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getIdUsuarioOrigem() {
        return idUsuarioOrigem;
    }

    public void setIdUsuarioOrigem(String idUsuarioOrigem) {
        this.idUsuarioOrigem = idUsuarioOrigem;
    }

    public String getIdUsuarioDestino() {
        return idUsuarioDestino;
    }

    public void setIdUsuarioDestino(String idUsuarioDestino) {
        this.idUsuarioDestino = idUsuarioDestino;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
