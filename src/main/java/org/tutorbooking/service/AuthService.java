package org.tutorbooking.service;

import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.dto.request.RegisterRequest;

public interface AuthService {
    @Transactional
    void registerUser(RegisterRequest signUpRequest);
}
