package org.tutorbooking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.tutorbooking.dto.request.GoogleLoginRequest;
import org.tutorbooking.dto.request.RegisterRequest;
import org.tutorbooking.dto.request.LoginRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.AuthResponse;
import org.tutorbooking.service.AuthService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService ;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity.ok(ApiResponse.builder().success(true).message("Đăng ký tài khoản thành công!").build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.loginUser(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.builder().success(false).message("Lỗi thực sự: " + e.getMessage()).build());
        }
    }

    @PostMapping("/google/login")
    public ResponseEntity<?> googleAuthenticateUser(@Valid @RequestBody GoogleLoginRequest googleLoginRequest) {
        try {
            AuthResponse authResponse = authService.googleLogin(googleLoginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody org.tutorbooking.dto.request.RefreshTokenRequest request) {
        try {
            AuthResponse authResponse = authService.refreshToken(request);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // Lấy email của user đang gọi request từ SecurityContext
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            authService.logout(auth.getName());
            
            return ResponseEntity.ok(ApiResponse.builder().success(true).message("Đăng xuất thành công!").build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody org.tutorbooking.dto.request.ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request);
            return ResponseEntity.ok(ApiResponse.builder().success(true).message("Vui lòng kiểm tra email để nhận mã xác nhận đặt lại mật khẩu.").build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody org.tutorbooking.dto.request.ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(ApiResponse.builder().success(true).message("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại!").build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody org.tutorbooking.dto.request.ChangePasswordRequest request) {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName(); 
            
            authService.changePassword(email, request);
            
            return ResponseEntity.ok(ApiResponse.builder().success(true).message("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.").build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.builder().success(false).message("Có lỗi xảy ra trong quá trình đổi mật khẩu!").build());
        }
    }
}
