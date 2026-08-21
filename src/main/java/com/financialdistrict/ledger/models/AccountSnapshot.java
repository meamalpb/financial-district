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

// Point-in-time capture of a ManAccount's state immediately after a buy is
// applied: cost basis, market value, and derived gain/gainPercent as they
// stood at that transaction, rather than only the live/current figures on
// ManAccount. transactionId is still stored as a plain column, not a JPA
// relation, since a snapshot outliving its source Transaction is harmless.
// manAccount is a real FK (man_account_id, ON DELETE CASCADE) so a snapshot
// can never outlive/orphan from its ManAccount.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account_snapshots")
public class AccountSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "man_account_id", nullable = false)
    private ManAccount manAccount;

    private String symbol;
    private Long transactionId;

    @Column(precision = 19, scale = 8)
    private BigDecimal price;
    @Column(precision = 19, scale = 8)
    private BigDecimal bankBalance;
    @Column(precision = 19, scale = 8)
    private BigDecimal sharesOwned;
    @Column(precision = 19, scale = 8)
    private BigDecimal costBasis;
    @Column(precision = 19, scale = 8)
    private BigDecimal marketValue;
    @Column(precision = 19, scale = 8)
    private BigDecimal gain;
    @Column(precision = 19, scale = 8)
    private BigDecimal gainPercent;

    private LocalDateTime timestamp;
}
