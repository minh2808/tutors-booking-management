package org.tutorbooking.service.Impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Parent;
import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.User;
import org.tutorbooking.domain.enums.AuthProvider;
import org.tutorbooking.domain.enums.Role;
import org.tutorbooking.dto.request.GoogleLoginRequest;
import org.tutorbooking.dto.request.LoginRequest;
import org.tutorbooking.dto.request.RegisterRequest;
import org.tutorbooking.dto.response.AuthResponse;
import org.tutorbooking.repository.ParentRepository;
import org.tutorbooking.repository.TutorRepository;
import org.tutorbooking.repository.UserRepository;
import org.tutorbooking.security.GoogleTokenVerifier;
import org.tutorbooking.security.JwtTokenProvider;
import org.tutorbooking.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    @Transactional
    @Override
    public void registerUser(RegisterRequest signUpRequest) {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .fullName(signUpRequest.getFullName())
                .phone(signUpRequest.getPhone())
                .role(signUpRequest.getRole())
                .authProvider(AuthProvider.LOCAL)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        if (signUpRequest.getRole() == Role.TUTOR) {
            Tutor tutor = Tutor.builder()
                    .user(savedUser)
                    .approvalStatus("pending")
                    .build();
            tutorRepository.save(tutor);
        } else if (signUpRequest.getRole() == Role.PARENT) {
            Parent parent = Parent.builder()
                    .user(savedUser)
                    .build();
            parentRepository.save(parent);
        }
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy User sau khi đăng nhập"));

        user.setRefreshToken(refreshToken);
        userRepository.save(user);


        return AuthResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest googleLoginRequest) {

        GoogleIdToken.Payload payload = googleTokenVerifier.verify(googleLoginRequest.getIdToken());
        if (payload == null) {
            throw new RuntimeException("Lỗi: Token Google không hợp lệ hoặc đã hết hạn!");
        }
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {

            Role role = googleLoginRequest.getRole() != null ? googleLoginRequest.getRole() : Role.PARENT;

            user = User.builder()
                    .email(email)
                    .fullName(name)
                    .avatarUrl(pictureUrl)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Mật khẩu rác vì mượn acc Google rồi
                    .role(role)
                    .authProvider(AuthProvider.GOOGLE)
                    .isActive(true)
                    .build();
            user = userRepository.save(user);

            if (role == Role.TUTOR) {
                Tutor tutor = Tutor.builder()
                        .user(user)
                        .approvalStatus("pending")
                        .build();
                tutorRepository.save(tutor);
            } else {
                Parent parent = Parent.builder()
                        .user(user)
                        .build();
                parentRepository.save(parent);
            }
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        org.springframework.security.core.userdetails.User userPrincipal = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), Collections.singletonList(authority));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}