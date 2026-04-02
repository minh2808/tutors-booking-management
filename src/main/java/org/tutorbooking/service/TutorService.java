package org.tutorbooking.service;

import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.TutorSubject;
import org.tutorbooking.dto.request.UpdateTutorRequest;
import org.tutorbooking.dto.request.SubjectRequest;
import org.tutorbooking.dto.response.TutorDetailResponse;
import java.util.List;

public interface TutorService {
    TutorDetailResponse getTutorDetail(Long tutorId);

    Tutor getMyProfile(Long userId);

    void updateProfile(Long userId, UpdateTutorRequest req);

    void updateSubjects(Long userId, List<SubjectRequest> reqs);

    List<TutorSubject> getSubjects(Long tutorId);
}
