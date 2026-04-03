package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Booking;
import org.tutorbooking.domain.entity.Session;
import org.tutorbooking.domain.entity.User;
import org.tutorbooking.domain.enums.SessionStatus;
import org.tutorbooking.dto.response.PageResponse;
import org.tutorbooking.dto.response.SessionDetailResponse;
import org.tutorbooking.exception.ResourceNotFoundException;
import org.tutorbooking.repository.SessionRepository;
import org.tutorbooking.repository.UserRepository;
import org.tutorbooking.service.SessionService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Override
    public PageResponse<SessionDetailResponse> getSessions(Long userId, String role,
                                                            LocalDate startDate, LocalDate endDate,
                                                            SessionStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Session> sessionPage;

        if ("ADMIN".equalsIgnoreCase(role)) {
            sessionPage = sessionRepository.findAllSessionsByFilters(startDate, endDate, status, pageable);
        } else {
            sessionPage = sessionRepository.findSessionsByFilters(userId, startDate, endDate, status, pageable);
        }

        List<SessionDetailResponse> content = sessionPage.getContent().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        return PageResponse.<SessionDetailResponse>builder()
                .content(content)
                .page(sessionPage.getNumber())
                .size(sessionPage.getSize())
                .totalElements(sessionPage.getTotalElements())
                .totalPages(sessionPage.getTotalPages())
                .last(sessionPage.isLast())
                .build();
    }

    @Override
    public SessionDetailResponse getSessionById(Long userId, String role, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!"ADMIN".equalsIgnoreCase(role)) {
            Booking booking = session.getBooking();
            boolean isParent = booking.getParent().getUser().getId().equals(userId);
            boolean isTutor = booking.getTutor().getUser().getId().equals(userId);
            if (!isParent && !isTutor) {
                throw new AccessDeniedException("You don't have permission to view this session");
            }
        }

        return toDetailResponse(session);
    }

    @Override
    @Transactional
    public SessionDetailResponse confirmSession(Long userId, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        Booking booking = session.getBooking();

        // Chỉ Tutor liên quan mới được confirm
        if (!booking.getTutor().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Only the assigned tutor can confirm this session");
        }

        // Chỉ PENDING mới confirm được
        if (session.getStatus() != SessionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING sessions can be confirmed. Current status: " + session.getStatus());
        }

        session.setStatus(SessionStatus.CONFIRMED);
        sessionRepository.save(session);

        return toDetailResponse(session);
    }

    @Override
    @Transactional
    public SessionDetailResponse cancelSession(Long userId, Long sessionId, String cancelReason) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        Booking booking = session.getBooking();

        // PH hoặc GS liên quan mới được hủy
        boolean isParent = booking.getParent().getUser().getId().equals(userId);
        boolean isTutor = booking.getTutor().getUser().getId().equals(userId);
        if (!isParent && !isTutor) {
            throw new AccessDeniedException("You don't have permission to cancel this session");
        }

        // Không hủy session đã COMPLETED hoặc đã CANCELLED
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed session");
        }
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new IllegalStateException("Session is already cancelled");
        }

        // Ghi nhận ai hủy + lý do
        User cancelledByUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        session.setStatus(SessionStatus.CANCELLED);
        session.setCancelledBy(cancelledByUser);
        session.setCancelReason(cancelReason);
        sessionRepository.save(session);

        return toDetailResponse(session);
    }

    private SessionDetailResponse toDetailResponse(Session session) {
        Booking booking = session.getBooking();
        return SessionDetailResponse.builder()
                .id(session.getId())
                .bookingId(booking.getId())
                .sessionDate(session.getSessionDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .status(session.getStatus())
                .tutorName(booking.getTutor().getUser().getFullName())
                .subjectName(booking.getSubject().getName())
                .studentName(booking.getStudent() != null ? booking.getStudent().getFullName() : null)
                .teachingMode(booking.getTeachingMode().name())
                .gradeLevel(String.valueOf(booking.getGradeLevel()))
                .cancelledByName(session.getCancelledBy() != null ? session.getCancelledBy().getFullName() : null)
                .cancelReason(session.getCancelReason())
                .build();
    }
}
