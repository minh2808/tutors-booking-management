package org.tutorbooking.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tutorbooking.domain.enums.TutorApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TutorApplicationResponse {
    private Long id;

    // Thông tin yêu cầu
    private Long requestId;
    private Long parentId;
    private String parentName;
    private Long subjectId;
    private String subjectName;
    private Byte gradeLevel;

    // Thông tin gia sư
    private Long tutorId;
    private String tutorName;
    private String tutorEmail;
    private String tutorPhone;
    private String tutorEducationLevel;
    private String tutorExperience;

    // Thông tin ứng tuyển
    private BigDecimal proposedPrice;
    private String coverLetter;
    private TutorApplicationStatus status;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
}
