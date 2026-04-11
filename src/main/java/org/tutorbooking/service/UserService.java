package org.tutorbooking.service;

import org.springframework.web.multipart.MultipartFile;
import org.tutorbooking.dto.request.UpdateProfileRequest;
import org.tutorbooking.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateProfileRequest request);

    UserProfileResponse updateAvatar(String email, MultipartFile file);

    void changePassword(String email, org.tutorbooking.dto.request.ChangePasswordRequest request);
}