package org.tutorbooking.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.User;
import org.tutorbooking.dto.request.UpdateProfileRequest;
import org.tutorbooking.dto.response.UserProfileResponse;
import org.tutorbooking.dto.request.ChangePasswordRequest;
import org.tutorbooking.repository.UserRepository;
import org.tutorbooking.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tutorbooking.domain.enums.AuthProvider;
import org.tutorbooking.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.tutorbooking.dto.response.UserAdminResponse;
import org.springframework.web.multipart.MultipartFile;
import org.tutorbooking.service.CloudinaryService;
import java.io.IOException;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public UserProfileResponse getMyProfile(String email) {
        System.out.println(">>> DEBUG: Hệ thống đang dùng Email này để tìm trong DB: [" + email + "]");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy user với email: " + email));

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy người dùng!"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        userRepository.save(user);

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không được để trống");
        }

        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            user.setAvatarUrl(imageUrl);
            userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Không thể xử lý và tải ảnh lên phần mềm lưu trữ: " + e.getMessage());
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng trong hệ thống."));

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new RuntimeException("Tài khoản liên kết với Google không thể đổi mật khẩu theo cách này!");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    @Override
    public Page<UserAdminResponse> getAllUsersForAdmin(Role role, Boolean isActive, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findUsersByFilters(role, isActive, keyword, pageable);
        return users.map(user -> UserAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .authProvider(user.getAuthProvider())
                .createdAt(user.getCreatedAt())
                .build());
    }

    @Override
    @Transactional
    public void disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với id: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }
}