package org.tutorbooking.service;

import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.TutorSubject;
import org.tutorbooking.dto.request.UpdateTutorRequest;
import org.tutorbooking.dto.request.SubjectRequest;
import org.tutorbooking.dto.response.TutorDetailResponse;
import java.util.List;

public interface TutorService {
    // lấy detail
    TutorDetailResponse getTutorDetail(Long tutorId);

    // lấy profile của mình
    Tutor getMyProfile(Long userId);

    // update hồ sơ
    void updateProfile(Long userId, UpdateTutorRequest req);

    // update subjects
    void updateSubjects(Long userId, List<SubjectRequest> reqs);

    // lấy subjects
    List<TutorSubject> getSubjects(Long tutorId);
}
