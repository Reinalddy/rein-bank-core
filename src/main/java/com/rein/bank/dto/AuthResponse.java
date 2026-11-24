package com.rein.bank.dto;

public record AuthResponse(
    String token,
    String type,
    String email,
    String fullName
) {}