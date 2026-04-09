package org.tutorbooking.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TutorSubjectResponse {
    private Long id;
    private Long subjectId;
    private String subjectName; // Fetch from Subject to display on UI
    private Integer gradeLevel;
    private Long pricePerSession;
}
