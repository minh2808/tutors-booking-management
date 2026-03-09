package org.tutorbooking.service;

import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.dto.request.RegisterRequest;

import org.tutorbooking.dto.request.LoginRequest;
import org.tutorbooking.dto.response.AuthResponse;

public interface AuthService {
    @Transactional
    void registerUser(RegisterRequest signUpRequest);

    AuthResponse loginUser(LoginRequest loginRequest);
}
