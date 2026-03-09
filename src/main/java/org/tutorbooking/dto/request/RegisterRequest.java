package org.tutorbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.tutorbooking.domain.enums.Role;


@Data
public class RegisterRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại (VN) không hợp lệ")
    private String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải dài ít nhất 8 ký tự")
    private String password;

    @NotNull(message = "Vui lòng chọn vai trò (TUTOR hoặc PARENT)")
    private Role role;
}

