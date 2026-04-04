package org.tutorbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SessionCancelRequest {
    @NotBlank(message = "Cancel reason is required")
    private String cancelReason;
}
