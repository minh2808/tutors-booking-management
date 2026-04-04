package org.tutorbooking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long bookingId;
    private String parentName;
    private String tutorName;
    private String subjectName;
    private Byte rating;
    private String comment;
    private LocalDateTime createdAt;
}
