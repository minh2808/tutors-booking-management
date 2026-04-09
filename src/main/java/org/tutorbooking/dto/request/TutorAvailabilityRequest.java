package org.tutorbooking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

@Data
public class TutorAvailabilityRequest {

    @NotNull(message = "Day of week cannot be null")
    private Integer dayOfWeek;

    @NotNull(message = "Start time cannot be null")
    private LocalTime startTime;

    @NotNull(message = "End time cannot be null")
    private LocalTime endTime;
    
    private Boolean isActive = true;
}
