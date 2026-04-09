package org.tutorbooking.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectTutorRequest {
    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;
}