package org.tutorbooking.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.tutorbooking.domain.enums.TeachingMode;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorRequestUpdateRequest {

    private BigDecimal desiredPrice;

    private TeachingMode teachingMode;

    @Size(max = 255, message = "Preferred area quá dài")
    private String preferredArea;

    @Size(max = 2000, message = "Schedule note quá dài")
    private String scheduleNote;

    private Byte sessionsPerWeek;
}
