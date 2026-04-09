package org.tutorbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectCreateRequest {

    @NotBlank(message = "Tên môn học không được để trống")
    private String name;

    private String description;
}
