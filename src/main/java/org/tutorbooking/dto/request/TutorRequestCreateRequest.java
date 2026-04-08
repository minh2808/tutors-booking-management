package org.tutorbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.tutorbooking.domain.enums.TeachingMode;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorRequestCreateRequest {

    @NotNull(message = "Subject ID không được null")
    private Long subjectId;

    @NotNull(message = "Grade level không được null")
    @Min(value = 1, message = "Grade phải >= 1")
    @Max(value = 12, message = "Grade phải <= 12")
    private Byte gradeLevel;

    @Positive(message = "Giá mong muốn phải > 0")
    private BigDecimal desiredPrice;

    @NotNull(message = "Teaching mode không được null")
    private TeachingMode teachingMode;

    @Size(max = 255, message = "Preferred area quá dài")
    private String preferredArea;

    @Size(max = 2000, message = "Schedule note quá dài")
    private String scheduleNote;

    @Min(value = 1, message = "Sessions per week phải >= 1")
    @Max(value = 7, message = "Sessions per week phải <= 7")
    private Byte sessionsPerWeek = 1;
}
