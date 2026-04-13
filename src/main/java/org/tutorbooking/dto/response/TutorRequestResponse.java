package org.tutorbooking.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tutorbooking.domain.enums.TeachingMode;
import org.tutorbooking.domain.enums.TutorRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TutorRequestResponse {
    private Long id;
    private Long parentId;
    private String parentName;
    private String parentPhone;
    private String parentEmail;

    private Long subjectId;
    private String subjectName;
    
    private Long studentId;
    private String studentName;
    private Byte gradeLevel;

    private BigDecimal desiredPrice;
    private TeachingMode teachingMode;
    private String preferredArea;
    private String scheduleNote;
    private Byte sessionsPerWeek;

    private TutorRequestStatus status;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long applicantsCount; // số ứng tuyển
}
