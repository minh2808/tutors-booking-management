package org.tutorbooking.service;

import org.tutorbooking.dto.request.TutorAvailabilityRequest;
import org.tutorbooking.dto.request.TutorSubjectRequest;
import org.tutorbooking.dto.response.TutorAvailabilityResponse;
import org.tutorbooking.dto.response.TutorSubjectResponse;

import java.util.List;

public interface TutorPortfolioService {

    // --- Tutor Subject ---
    List<TutorSubjectResponse> getTutorSubjects(Long tutorId);

    TutorSubjectResponse addTutorSubject(Long userId, TutorSubjectRequest request);

    TutorSubjectResponse updateTutorSubject(Long userId, Long subjectId, TutorSubjectRequest request);

    void removeTutorSubject(Long userId, Long subjectId);

    // --- Tutor Availability ---
    List<TutorAvailabilityResponse> getTutorAvailability(Long tutorId);

    TutorAvailabilityResponse addTutorAvailability(Long userId, TutorAvailabilityRequest request);

    TutorAvailabilityResponse updateTutorAvailability(Long userId, Long availabilityId, TutorAvailabilityRequest request);

    void removeTutorAvailability(Long userId, Long availabilityId);
}
