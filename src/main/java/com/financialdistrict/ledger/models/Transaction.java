package com.financialdistrict.ledger.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "man_account_id", nullable = false)
    private ManAccount manAccount;

    private String symbol;

    @Column(precision = 19, scale = 8)
    private BigDecimal amount;
    @Column(precision = 19, scale = 8)
    private BigDecimal shares;
    @Column(precision = 19, scale = 8)
    private BigDecimal price;

    private String transactionType;

    private LocalDateTime timestamp;
}
