package org.tutorbooking.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tutorbooking.domain.enums.SessionStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class SessionResponse {
    private Long id;
    private Long bookingId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SessionStatus status;
}
