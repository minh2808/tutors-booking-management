package org.tutorbooking.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tutorbooking.domain.enums.SessionStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class SessionDetailResponse {
    private Long id;
    private Long bookingId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SessionStatus status;

    // Thông tin bổ sung cho Calendar view
    private String tutorName;
    private String subjectName;
    private String studentName;
    private String teachingMode;
    private String gradeLevel;

    // Thông tin hủy (nếu có)
    private String cancelledByName;
    private String cancelReason;
}
