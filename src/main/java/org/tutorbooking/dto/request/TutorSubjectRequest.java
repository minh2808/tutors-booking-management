package org.tutorbooking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TutorSubjectRequest {

    private Long subjectId; // null when updating, required when creating

    @NotNull(message = "Grade level cannot be null")
    @Min(value = 1, message = "Grade level must be at least 1")
    private Integer gradeLevel;

    @NotNull(message = "Price per session cannot be null")
    @Min(value = 0, message = "Price cannot be negative")
    private Long pricePerSession;
}
