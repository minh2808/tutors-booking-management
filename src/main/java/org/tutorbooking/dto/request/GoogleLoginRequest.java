package org.tutorbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.tutorbooking.domain.enums.Role;

@Data
public class GoogleLoginRequest {

    @NotBlank(message = "ID Token từ Google không được để trống")
    private String idToken;


    private Role role;
}
