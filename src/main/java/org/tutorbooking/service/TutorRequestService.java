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

    TutorRequestResponse createRequest(Long userId, TutorRequestCreateRequest req);

    TutorRequestResponse getRequestDetail(Long requestId);

    List<TutorRequestResponse> getMyRequests(Long userId);

    void updateRequest(Long requestId, Long userId, TutorRequestUpdateRequest req);

    void cancelRequest(Long requestId, Long userId);

    List<TutorRequestResponse> getAllRequests();

    TutorApplication applyForRequest(Long requestId, Long userId, TutorApplicationCreateRequest req);

    void withdrawApplication(Long applicationId, Long userId);

    List<TutorApplicationResponse> getApplicationsForRequest(Long requestId, Long userId);

    List<TutorApplicationResponse> getMyApplications(Long userId);

    void acceptApplication(Long applicationId, Long userId);

    void rejectApplication(Long applicationId, Long userId);

    TutorApplicationResponse getApplicationDetail(Long applicationId);
}