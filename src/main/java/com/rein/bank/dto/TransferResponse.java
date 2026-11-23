package com.rein.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
    String referenceId,
    String fromAccountNumber,
    String toAccountNumber,
    BigDecimal amount,
    BigDecimal remainingBalance,
    String status,
    LocalDateTime timestamp
) {}