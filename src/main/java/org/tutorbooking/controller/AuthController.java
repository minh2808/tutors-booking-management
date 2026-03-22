package org.tutorbooking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.tutorbooking.dto.request.GoogleLoginRequest;
import org.tutorbooking.dto.request.RegisterRequest;
import org.tutorbooking.dto.request.LoginRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.AuthResponse;
import org.tutorbooking.service.AuthService;
import org.tutorbooking.security.JwtTokenProvider; 
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider; 
    @Autowired
    private org.tutorbooking.repository.UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity
                .ok(ApiResponse.builder().success(true).message("Đăng ký tài khoản thành công!").build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.loginUser(loginRequest);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Đăng nhập thành công!")
                .data(authResponse) 
                .build());
    }

    @PostMapping("/google/login")
    public ResponseEntity<?> googleAuthenticateUser(@Valid @RequestBody GoogleLoginRequest googleLoginRequest) {
        AuthResponse authResponse = authService.googleLogin(googleLoginRequest);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Đăng nhập Google thành công!")
                .data(authResponse)
                .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody org.tutorbooking.dto.request.RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Làm mới token thành công!")
                .data(authResponse)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        authService.logout(auth.getName());
        
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Đăng xuất thành công!").build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody org.tutorbooking.dto.request.ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Vui lòng kiểm tra email để nhận mã xác nhận đặt lại mật khẩu.").build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody org.tutorbooking.dto.request.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại!").build());
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody org.tutorbooking.dto.request.ChangePasswordRequest request) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); 

        authService.changePassword(email, request);
        
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.").build());
    }
}

