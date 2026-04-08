package org.tutorbooking.service;

import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.TutorSubject;
import org.tutorbooking.dto.request.UpdateTutorRequest;
import org.tutorbooking.dto.request.SubjectRequest;
import org.tutorbooking.dto.response.TutorDetailResponse;
import org.tutorbooking.dto.response.TutorReviewSummaryResponse;

import java.util.List;
import org.springframework.data.domain.Page;

public interface TutorService {
    TutorDetailResponse getTutorDetail(Long tutorId);

    Tutor getMyProfile(Long userId);

    void updateProfile(Long userId, UpdateTutorRequest req);

    void updateSubjects(Long userId, List<SubjectRequest> reqs);

    List<TutorSubject> getSubjects(Long tutorId);

    Page<TutorDetailResponse> searchTutors(Long subjectId, Integer grade, Long minPrice, Long maxPrice, String teachingMode, int page, int size);

    TutorReviewSummaryResponse getTutorReviews(Long tutorId, int page, int size);
}
