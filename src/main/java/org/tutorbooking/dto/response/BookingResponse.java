package org.tutorbooking.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tutorbooking.domain.enums.BookingStatus;
import org.tutorbooking.domain.enums.TeachingMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private Long parentId;
    private Long tutorId;
    private Long subjectId;
    private Long studentId;
    private Byte gradeLevel;
    private BigDecimal pricePerSession;
    private TeachingMode teachingMode;
    private Boolean isRecurring;
    private java.util.List<ScheduleResponseItem> schedules;

    @Data
    @Builder
    public static class ScheduleResponseItem {
        private Long id;
        private Integer dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
    }

    private LocalDate recurringStartDate;
    private LocalDate recurringEndDate;
    private BookingStatus status;
    private List<SessionResponse> sessions;
}
