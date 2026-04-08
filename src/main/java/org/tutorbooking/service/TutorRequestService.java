package org.tutorbooking.service;

import org.tutorbooking.domain.entity.TutorApplication;
import org.tutorbooking.domain.entity.TutorRequest;
import org.tutorbooking.dto.request.TutorApplicationCreateRequest;
import org.tutorbooking.dto.request.TutorRequestCreateRequest;
import org.tutorbooking.dto.request.TutorRequestUpdateRequest;
import org.tutorbooking.dto.response.TutorApplicationResponse;
import org.tutorbooking.dto.response.TutorRequestResponse;

import java.util.List;

public interface TutorRequestService {

    TutorRequest createRequest(Long parentId, TutorRequestCreateRequest req);

    TutorRequestResponse getRequestDetail(Long requestId);

    List<TutorRequestResponse> getMyRequests(Long parentId);

    void updateRequest(Long requestId, Long parentId, TutorRequestUpdateRequest req);

    void cancelRequest(Long requestId, Long parentId);

    List<TutorRequestResponse> getAllRequests();

    TutorApplication applyForRequest(Long requestId, Long tutorId, TutorApplicationCreateRequest req);

    void withdrawApplication(Long applicationId, Long tutorId);

    List<TutorApplicationResponse> getApplicationsForRequest(Long requestId, Long parentId);

    List<TutorApplicationResponse> getMyApplications(Long tutorId);

    void acceptApplication(Long applicationId, Long parentId);

    void rejectApplication(Long applicationId, Long parentId);

    TutorApplicationResponse getApplicationDetail(Long applicationId);
}