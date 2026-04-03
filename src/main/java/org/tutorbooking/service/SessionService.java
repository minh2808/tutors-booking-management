package org.tutorbooking.service;

import org.tutorbooking.domain.enums.SessionStatus;
import org.tutorbooking.dto.response.PageResponse;
import org.tutorbooking.dto.response.SessionDetailResponse;

import java.time.LocalDate;

public interface SessionService {
    PageResponse<SessionDetailResponse> getSessions(Long userId, String role,
                                                     LocalDate startDate, LocalDate endDate,
                                                     SessionStatus status, int page, int size);
    SessionDetailResponse getSessionById(Long userId, String role, Long sessionId);
    SessionDetailResponse confirmSession(Long userId, Long sessionId);
    SessionDetailResponse cancelSession(Long userId, Long sessionId, String cancelReason);
}
