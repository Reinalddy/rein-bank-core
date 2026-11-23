package com.rein.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateAccountRequest(
    @NotBlank(message = "Nama pemilik rekening wajib diisi")
    String ownerName,

    @PositiveOrZero(message = "Setoran awal tidak boleh negatif")
    BigDecimal initialDeposit
) {}