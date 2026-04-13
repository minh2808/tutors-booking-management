package org.tutorbooking.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

@Data
public class TutorAvailabilityRequest {

    @NotNull(message = "Day of week cannot be null")
    private Integer dayOfWeek;

    @NotNull(message = "Start time cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime startTime;

    @NotNull(message = "End time cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime endTime;

    private Boolean isActive = true;
}
