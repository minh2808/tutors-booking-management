package org.tutorbooking.service;

import org.tutorbooking.dto.request.UpdateProfileRequest;
import org.tutorbooking.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateProfileRequest request);
}