package org.tutorbooking.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import org.tutorbooking.domain.enums.Role;
import org.tutorbooking.dto.request.UpdateProfileRequest;
import org.tutorbooking.dto.response.UserAdminResponse;
import org.tutorbooking.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateProfileRequest request);

    UserProfileResponse updateAvatar(String email, MultipartFile file);

    void changePassword(String email, org.tutorbooking.dto.request.ChangePasswordRequest request);

    Page<UserAdminResponse> getAllUsersForAdmin(Role role, Boolean isActive, String keyword, int page, int size);

    void disableUser(Long id);
}