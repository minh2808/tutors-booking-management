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
import org.tutorbooking.domain.enums.BookingStatus;
import org.tutorbooking.domain.enums.SessionStatus;
import org.tutorbooking.dto.response.PageResponse;
import org.tutorbooking.dto.response.SessionDetailResponse;
import org.tutorbooking.exception.ResourceNotFoundException;
import org.tutorbooking.repository.SessionRepository;
import org.tutorbooking.repository.UserRepository;
import org.tutorbooking.service.EmailService;
import org.tutorbooking.service.SessionService;

import java.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Buổi học."));

        if (!"ADMIN".equalsIgnoreCase(role)) {
            Booking booking = session.getBooking();
            boolean isParent = booking.getParent().getUser().getId().equals(userId);
            boolean isTutor = booking.getTutor().getUser().getId().equals(userId);
            if (!isParent && !isTutor) {
                throw new AccessDeniedException("Bạn không có quyền xem thông tin của buổi học này.");
            }
        }

        return toDetailResponse(session);
    }

    @Override
    @Transactional
    public SessionDetailResponse confirmSession(Long userId, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Buổi học."));

        Booking booking = session.getBooking();

        // Chỉ Tutor liên quan mới được confirm
        if (!booking.getTutor().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Chi gia sư được giao lớp mới có quyền xác nhận buổi học này.");
        }

        // Chỉ PENDING mới confirm được
        if (session.getStatus() != SessionStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể xác nhận các buổi học đang ở Mức Chờ (PENDING). Trạng thái hiện tại: " + session.getStatus());
        }

        session.setStatus(SessionStatus.CONFIRMED);
        sessionRepository.save(session);

        // Gửi email bất đồng bộ cho Phụ huynh
        User parentUser = booking.getParent().getUser();
        emailService.sendSessionConfirmedEmail(
                parentUser.getEmail(),
                parentUser.getFullName(),
                booking.getTutor().getUser().getFullName(),
                booking.getSubject().getName(),
                session.getSessionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                session.getStartTime().toString(),
                session.getEndTime().toString()
        );

        return toDetailResponse(session);
    }

    @Override
    @Transactional
    public SessionDetailResponse completeSession(Long userId, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Buổi học."));

        Booking booking = session.getBooking();

        if (!booking.getTutor().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Chỉ gia sư được giao lớp mới có quyền bấm Hoàn thành buổi học.");
        }

        if (session.getStatus() != SessionStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể hoàn thành các buổi học đã được xác nhận (CONFIRMED). Trạng thái hiện tại: " + session.getStatus());
        }

        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);

        // Kiểm tra xem tất cả các buổi học của khóa này đã kết thúc chưa
        List<Session> allSessions = sessionRepository.findByBookingId(booking.getId());
        boolean isAllDone = allSessions.stream()
                .allMatch(s -> s.getStatus() == SessionStatus.COMPLETED || s.getStatus() == SessionStatus.CANCELLED);
        
        if (isAllDone) {
            boolean hasCompleted = allSessions.stream().anyMatch(s -> s.getStatus() == SessionStatus.COMPLETED);
            
            if (hasCompleted) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else {
                booking.setStatus(BookingStatus.CANCELLED);
            }
        }

        return toDetailResponse(session);
    }

    @Override
    @Transactional
    public SessionDetailResponse cancelSession(Long userId, Long sessionId, String cancelReason) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Buổi học."));

        Booking booking = session.getBooking();

        // PH hoặc GS liên quan mới được hủy
        boolean isParent = booking.getParent().getUser().getId().equals(userId);
        boolean isTutor = booking.getTutor().getUser().getId().equals(userId);
        if (!isParent && !isTutor) {
            throw new AccessDeniedException("Bạn không có quyền Hủy buổi học của người khác.");
        }

        // Không hủy session đã COMPLETED hoặc đã CANCELLED
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Không thể hủy buổi học đã diễn ra và Hoàn thành.");
        }
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new IllegalStateException("Buổi học này đã bị Hủy từ trước rồi.");
        }

        // Ghi nhận ai hủy + lý do
        User cancelledByUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản người dùng thao tác."));

        session.setStatus(SessionStatus.CANCELLED);
        session.setCancelledBy(cancelledByUser);
        session.setCancelReason(cancelReason);
        sessionRepository.save(session);

        // Gửi email báo Hủy cho bên còn lại
        User receiverUser = isParent ? booking.getTutor().getUser() : booking.getParent().getUser();
        String cancellerRoleName = isParent ? "Phụ huynh" : "Gia sư";
        
        emailService.sendSessionCancelledEmail(
                receiverUser.getEmail(),
                receiverUser.getFullName(),
                cancellerRoleName,
                booking.getSubject().getName(),
                session.getSessionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                session.getStartTime().toString(),
                session.getEndTime().toString(),
                cancelReason
        );

        // Kiểm tra xem tất cả các buổi học của khóa này đã kết thúc hoặc hủy hết chưa
        List<Session> allSessions = sessionRepository.findByBookingId(booking.getId());
        boolean isAllDone = allSessions.stream()
                .allMatch(s -> s.getStatus() == SessionStatus.COMPLETED || s.getStatus() == SessionStatus.CANCELLED);
        
        if (isAllDone) {
            boolean hasCompleted = allSessions.stream().anyMatch(s -> s.getStatus() == SessionStatus.COMPLETED);
            if (hasCompleted) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else {
                booking.setStatus(BookingStatus.CANCELLED);
            }
        }

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
