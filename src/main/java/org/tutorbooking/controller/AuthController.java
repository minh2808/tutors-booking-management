package org.tutorbooking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.tutorbooking.dto.request.GoogleLoginRequest;
import org.tutorbooking.dto.request.RegisterRequest;
import org.tutorbooking.dto.request.LoginRequest;
import org.tutorbooking.dto.request.ResetPasswordRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.AuthResponse;
import org.tutorbooking.service.AuthService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản thành công!"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.loginUser(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công!", authResponse));
    }

    @PostMapping("/google/login")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuthenticateUser(@Valid @RequestBody GoogleLoginRequest googleLoginRequest) {
        AuthResponse authResponse = authService.googleLogin(googleLoginRequest);
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra đăng nhập Google thành công!", authResponse));
    }

    @PostMapping("/google/register")
    public ResponseEntity<ApiResponse<AuthResponse>> googleRegisterUser(@Valid @RequestBody GoogleLoginRequest googleLoginRequest) {
        AuthResponse authResponse = authService.googleRegister(googleLoginRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản Google thành công!", authResponse));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody org.tutorbooking.dto.request.RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công!", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        authService.logout(auth.getName());
        
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công!"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody org.tutorbooking.dto.request.ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Vui lòng kiểm tra email để nhận mã xác nhận đặt lại mật khẩu."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại!"));
    }

}

