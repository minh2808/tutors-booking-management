package org.tutorbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectRequest {

    @NotNull(message = "SubjectId không được null")
    private Long subjectId;

    @NotNull(message = "Grade level không được null")
    @Min(value = 1, message = "Grade phải >= 1")
    @Max(value = 12, message = "Grade phải <= 12")
    private Integer gradeLevel;

    @NotNull(message = "Price không được null")
    @Min(value = 10000, message = "Giá phải >= 10k")
    private Long pricePerSession;
}