package org.tutorbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tutorbooking.domain.enums.AuthProvider;
import org.tutorbooking.domain.enums.Role;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private Boolean isActive;
    private AuthProvider authProvider;
    private LocalDateTime createdAt;
}
