package org.tutorbooking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import java.util.UUID;
import org.tutorbooking.dto.request.GoogleLoginRequest;
import org.tutorbooking.dto.request.RegisterRequest;
import org.tutorbooking.dto.request.LoginRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.AuthResponse;
import org.tutorbooking.repository.ParentRepository;
import org.tutorbooking.repository.TutorRepository;
import org.tutorbooking.service.AuthService;
import org.tutorbooking.security.JwtTokenProvider; // Đảm bảo đúng package của bạn

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private org.tutorbooking.repository.UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/google-success")
    public ResponseEntity<?> googleSuccess(Authentication authentication) {
        try {
            if (authentication == null)
                return ResponseEntity.status(401).body("Xác thực thất bại");

            String email, name, picture;
            if (authentication
                    .getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
                email = oAuth2User.getAttribute("email");
                name = oAuth2User.getAttribute("name");
                picture = oAuth2User.getAttribute("picture");
            } else {
                return ResponseEntity.status(401).body("Không lấy được thông tin từ Google");
            }

            // 1. Kiểm tra xem User đã có trong DB chưa
            var userOptional = userRepository.findByEmail(email);
            org.tutorbooking.domain.entity.User user;

            if (userOptional.isEmpty()) {
                // 2. NẾU CHƯA CÓ -> TỰ ĐỘNG TẠO MỚI (Mặc định là PARENT cho dễ test)
                user = org.tutorbooking.domain.entity.User.builder()
                        .email(email)
                        .fullName(name)
                        .avatarUrl(picture)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(org.tutorbooking.domain.enums.Role.TUTOR) // Mặc định Role
                        .authProvider(org.tutorbooking.domain.enums.AuthProvider.GOOGLE)
                        .isActive(true)
                        .build();
                user = userRepository.save(user);
                System.out.println(">>> Đã tự động lưu User mới: " + email);
            } else {
                user = userOptional.get();
                if (user.getPassword() == null) {
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    userRepository.save(user);
                }
            }

            // 3. Đóng gói lại thành User chuẩn của Spring Security để tạo JWT
            var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_" + user.getRole().name());
            var userPrincipal = new org.springframework.security.core.userdetails.User(
                    user.getEmail(), "", java.util.Collections.singletonList(authority));

            var finalAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userPrincipal, null, userPrincipal.getAuthorities());

            // 4. Tạo Token
            String jwt = jwtTokenProvider.generateToken(finalAuth);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", jwt);
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("message", "Đã lưu user và cấp Token thành công!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

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

    private void createProfileIfNotExists(org.tutorbooking.domain.entity.User user) {

        if (user.getRole() == org.tutorbooking.domain.enums.Role.TUTOR) {

            boolean exists = tutorRepository.existsByUserId(user.getId());

            if (!exists) {
                org.tutorbooking.domain.entity.Tutor tutor = org.tutorbooking.domain.entity.Tutor.builder()
                        .user(user)
                        .approvalStatus("pending")
                        .build();

                tutorRepository.save(tutor);
                System.out.println(">>> Đã tạo Tutor profile");
            }

        } else if (user.getRole() == org.tutorbooking.domain.enums.Role.PARENT) {

            boolean exists = parentRepository.existsByUserId(user.getId());

            if (!exists) {
                org.tutorbooking.domain.entity.Parent parent = org.tutorbooking.domain.entity.Parent.builder()
                        .user(user)
                        .build();

                parentRepository.save(parent);
                System.out.println(">>> Đã tạo Parent profile");
            }
        }
    }
}