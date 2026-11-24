package com.rein.bank.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ApplyLoanRequest(
    @NotBlank(message = "Nomor rekening pencairan wajib diisi") String accountNumber,

    @NotNull(message = "Nominal pinjaman wajib diisi") @Positive(message = "Nominal pinjaman harus lebih dari 0") BigDecimal principalAmount,

    @NotNull(message = "Tenor pinjaman wajib diisi") @Min(value = 1, message = "Tenor minimal 1 bulan") @Max(value = 36, message = "Tenor maksimal 36 bulan") Integer tenorMonths) {}
