package org.tutorbooking.service;

import org.tutorbooking.dto.request.UpdateProfileRequest;
import org.tutorbooking.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateProfileRequest request);

    org.tutorbooking.dto.response.UserProfileResponse updateAvatar(String email, org.springframework.web.multipart.MultipartFile file);

    void changePassword(String email, org.tutorbooking.dto.request.ChangePasswordRequest request);
}