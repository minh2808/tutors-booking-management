package org.tutorbooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.tutorbooking.domain.enums.TeachingMode;

import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class BookingCreateRequest {

    @NotNull(message = "Tutor ID cannot be null")
    private Long tutorId;

    @NotNull(message = "Subject ID cannot be null")
    private Long subjectId;

    private Long studentId; // nullable (if parent learns directly)

    @NotNull(message = "Grade level cannot be null")
    private Byte gradeLevel;

    @NotNull(message = "Teaching mode is required")
    private TeachingMode teachingMode;

    private Boolean isRecurring = false;

    @NotEmpty(message = "Bạn chưa chọn khung giờ nào")
    @Valid
    private java.util.List<ScheduleItem> schedules;

    @Data
    public static class ScheduleItem {
        @NotNull(message = "Day of week cannot be null")
        private Integer dayOfWeek;

        @NotNull(message = "Start time is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        private LocalTime startTime;

        @NotNull(message = "End time is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        private LocalTime endTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recurringStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recurringEndDate;
}
