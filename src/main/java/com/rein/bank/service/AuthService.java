package com.rein.bank.service;

import com.rein.bank.dto.AuthResponse;
import com.rein.bank.dto.LoginRequest;
import com.rein.bank.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}