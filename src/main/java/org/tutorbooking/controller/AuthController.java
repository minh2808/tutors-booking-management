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
import org.tutorbooking.security.JwtTokenProvider; // Đảm bảo đúng package của bạn

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

//    @GetMapping("/google-success")
//    public ResponseEntity<?> googleSuccess(Authentication authentication) {
//        try {
//            if (authentication == null)
//                return ResponseEntity.status(401).body("Xác thực thất bại");
//
//            String email, name, picture;
//            if (authentication
//                    .getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
//                email = oAuth2User.getAttribute("email");
//                name = oAuth2User.getAttribute("name");
//                picture = oAuth2User.getAttribute("picture");
//            } else {
//                return ResponseEntity.status(401).body("Không lấy được thông tin từ Google");
//            }
//
//            // 1. Kiểm tra xem User đã có trong DB chưa
//            var userOptional = userRepository.findByEmail(email);
//            org.tutorbooking.domain.entity.User user;
//
//            if (userOptional.isEmpty()) {
//                // 2. NẾU CHƯA CÓ -> TỰ ĐỘNG TẠO MỚI (Mặc định là PARENT cho dễ test)
//                user = org.tutorbooking.domain.entity.User.builder()
//                        .email(email)
//                        .fullName(name)
//                        .avatarUrl(picture)
//                        .role(org.tutorbooking.domain.enums.Role.PARENT) // Mặc định Role
//                        .authProvider(org.tutorbooking.domain.enums.AuthProvider.GOOGLE)
//                        .isActive(true)
//                        .build();
//                user = userRepository.save(user);
//                System.out.println(">>> Đã tự động lưu User mới: " + email);
//            } else {
//                user = userOptional.get();
//            }
//
//            // 3. Đóng gói lại thành User chuẩn của Spring Security để tạo JWT
//            var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority(
//                    "ROLE_" + user.getRole().name());
//            var userPrincipal = new org.springframework.security.core.userdetails.User(
//                    user.getEmail(), "", java.util.Collections.singletonList(authority));
//
//            var finalAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
//                    userPrincipal, null, userPrincipal.getAuthorities());
//
//            // 4. Tạo Token
//            String jwt = jwtTokenProvider.generateToken(finalAuth);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("accessToken", jwt);
//            response.put("email", user.getEmail());
//            response.put("role", user.getRole());
//            response.put("message", "Đã lưu user và cấp Token thành công!");
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
//        }
//    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity
                    .ok(ApiResponse.builder().success(true).message("Đăng ký tài khoản thành công!").build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.loginUser(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder().success(false).message("Lỗi: " + e.getMessage()).build());
        }
    }

    @PostMapping("/google/login")
    public ResponseEntity<?> googleAuthenticateUser(@Valid @RequestBody GoogleLoginRequest googleLoginRequest) {
        try {
            AuthResponse authResponse = authService.googleLogin(googleLoginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder().success(false).message(e.getMessage()).build());
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

