package com.rein.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
    @NotBlank(message = "Rekening pengirim wajib diisi")
    String fromAccountNumber,

    @NotBlank(message = "Rekening tujuan wajib diisi")
    String toAccountNumber,

    @NotNull(message = "Nominal transfer wajib diisi")
    @Positive(message = "Nominal transfer harus lebih dari 0")
    BigDecimal amount,

    String description
) {}