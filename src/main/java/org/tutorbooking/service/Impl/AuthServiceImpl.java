package org.tutorbooking.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Parent;
import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.User;
import org.tutorbooking.domain.enums.AuthProvider;
import org.tutorbooking.domain.enums.Role;
import org.tutorbooking.dto.request.LoginRequest;
import org.tutorbooking.dto.request.RegisterRequest;
import org.tutorbooking.dto.response.AuthResponse;
import org.tutorbooking.repository.ParentRepository;
import org.tutorbooking.repository.TutorRepository;
import org.tutorbooking.repository.UserRepository;
import org.tutorbooking.security.JwtTokenProvider;
import org.tutorbooking.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

        log.info("Hellooooooooooooooooooooo"+ loginRequest.getEmail());
        log.info(loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        log.info("nhảy vào đây");
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
}