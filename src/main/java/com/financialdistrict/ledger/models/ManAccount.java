package com.financialdistrict.ledger.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "man_accounts")
public class ManAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String manId;
    private String symbol;

    @Column(precision = 19, scale = 8)
    private BigDecimal bankBalance;
    @Column(precision = 19, scale = 8)
    private BigDecimal sharesOwned;
    @Column(precision = 19, scale = 8)
    private BigDecimal costBasis;
    @Column(precision = 19, scale = 8)
    private BigDecimal marketValue;

    private LocalDateTime updatedAt;
}
