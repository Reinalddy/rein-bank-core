package com.rein.bank.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 20)
    private String loanNumber;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal monthlyInterestRate;

    @Column(nullable = false)
    private Integer tenorMonths;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPayable;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyInstallment;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingBalance;

    @Column(nullable = false)
    private Integer paidInstallments;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.paidInstallments == null) {
            this.paidInstallments = 0;
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
    }
}
