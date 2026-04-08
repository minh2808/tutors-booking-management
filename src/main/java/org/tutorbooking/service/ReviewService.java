package org.tutorbooking.service;

import org.tutorbooking.dto.request.ReviewCreateRequest;
import org.tutorbooking.dto.response.PageResponse;
import org.tutorbooking.dto.response.ReviewResponse;

public interface ReviewService {
    ReviewResponse createReview(Long userId, ReviewCreateRequest request);
    PageResponse<ReviewResponse> getReviews(Long tutorId, int page, int size);
}
