package org.tutorbooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tutorbooking.dto.request.UpdateProfileRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.UserProfileResponse;
import org.tutorbooking.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thành công",
                userService.getMyProfile(authentication.getName())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công",
                userService.updateProfile(authentication.getName(), request)));
    }

    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh đại diện thành công",
                userService.updateAvatar(authentication.getName(), file)));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody org.tutorbooking.dto.request.ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công! Vui lòng đăng nhập lại."));
    }
}
