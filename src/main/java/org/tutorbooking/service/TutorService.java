package org.tutorbooking.service;

import org.tutorbooking.dto.request.UpdateTutorRequest;
import org.tutorbooking.dto.response.TutorDetailResponse;
import org.tutorbooking.dto.response.TutorReviewSummaryResponse;
import org.springframework.data.domain.Page;

public interface TutorService {
    TutorDetailResponse getTutorDetail(Long tutorId);
    
    TutorDetailResponse getMyTutorProfile(Long userId);

    void updateProfile(Long userId, UpdateTutorRequest req);

    Page<TutorDetailResponse> searchTutors(Long subjectId, Integer grade, Long minPrice, Long maxPrice, String teachingMode, int page, int size, String sortBy, String sortDirection);

    TutorReviewSummaryResponse getTutorReviews(Long tutorId, Integer rating, int page, int size);

    Page<TutorDetailResponse> getPendingTutors(int page, int size);
    void approveTutor(Long tutorId);
    void rejectTutor(Long tutorId, String reason);
}
