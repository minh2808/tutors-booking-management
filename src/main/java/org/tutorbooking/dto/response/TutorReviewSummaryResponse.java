package org.tutorbooking.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class TutorReviewSummaryResponse {
    private Double averageRating;      
    private Long totalReviews;         
    private Page<ReviewResponse> reviews; 
}