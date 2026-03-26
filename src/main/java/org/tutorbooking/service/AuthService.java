package org.tutorbooking.service;

import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.dto.request.GoogleLoginRequest;
import org.tutorbooking.dto.request.RegisterRequest;

import org.tutorbooking.dto.request.LoginRequest;
import org.tutorbooking.dto.response.AuthResponse;

public interface AuthService {
    @Transactional
    void registerUser(RegisterRequest signUpRequest);

    AuthResponse loginUser(LoginRequest loginRequest);


    AuthResponse googleLogin(GoogleLoginRequest googleLoginRequest);
    AuthResponse googleRegister(GoogleLoginRequest googleLoginRequest);

    AuthResponse refreshToken(org.tutorbooking.dto.request.RefreshTokenRequest request);
    void logout(String email);
    void forgotPassword(org.tutorbooking.dto.request.ForgotPasswordRequest request);
    void resetPassword(org.tutorbooking.dto.request.ResetPasswordRequest request);
    void createProfileIfNotExists(org.tutorbooking.domain.entity.User user);

    @Transactional
    void changePassword(String email, org.tutorbooking.dto.request.ChangePasswordRequest request);
}

