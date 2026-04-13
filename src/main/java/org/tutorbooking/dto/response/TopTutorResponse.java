package org.tutorbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopTutorResponse {
    private Long id;
    private String fullName;
    private String avatarUrl;
    private String email;
    private Double averageRating;
    private Long totalReviews;
}
