package org.tutorbooking.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Parent;
import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.User;
import org.tutorbooking.domain.enums.AuthProvider;
import org.tutorbooking.domain.enums.Role;
import org.tutorbooking.dto.request.RegisterRequest;
import org.tutorbooking.repository.ParentRepository;
import org.tutorbooking.repository.TutorRepository;
import org.tutorbooking.repository.UserRepository;
import org.tutorbooking.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private PasswordEncoder
            passwordEncoder;

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
}