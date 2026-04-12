package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.*;
import org.tutorbooking.domain.enums.BookingStatus;
import org.tutorbooking.domain.enums.SessionStatus;
import org.tutorbooking.dto.request.BookingCreateRequest;
import org.tutorbooking.dto.response.BookingResponse;
import org.tutorbooking.dto.response.PageResponse;
import org.tutorbooking.dto.response.SessionResponse;
import org.tutorbooking.exception.ResourceNotFoundException;
import org.tutorbooking.repository.*;
import org.tutorbooking.service.BookingService;
import org.tutorbooking.service.EmailService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;
    private final TutorRepository tutorRepository;
    private final SubjectRepository subjectRepository;
    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final TutorSubjectRepository tutorSubjectRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public BookingResponse createBooking(Long userId, BookingCreateRequest request) {

        Parent parent = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ Phụ huynh của người dùng này"));

        Tutor tutor = tutorRepository.findById(request.getTutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Gia sư"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Môn học"));

        Student student = null;
        if (request.getStudentId() != null) {
            student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Hồ sơ Học sinh"));
        }

        TutorSubject tutorSubject = tutorSubjectRepository
                .findByTutorIdAndSubjectIdAndGradeLevel(request.getTutorId(), request.getSubjectId(), (int) request.getGradeLevel())
                .orElseThrow(() -> new ResourceNotFoundException("Gia sư không dạy môn này ở cấp lớp yêu cầu"));

        BigDecimal price = BigDecimal.valueOf(tutorSubject.getPricePerSession());

        Booking booking = Booking.builder()
                .parent(parent)
                .tutor(tutor)
                .subject(subject)
                .student(student)
                .gradeLevel(request.getGradeLevel())
                .pricePerSession(price)
                .teachingMode(request.getTeachingMode())
                .isRecurring(request.getIsRecurring())
                .recurringStartDate(request.getRecurringStartDate())
                .recurringEndDate(request.getRecurringEndDate())
                .status(BookingStatus.ACTIVE)
                .schedules(new ArrayList<>())
                .build();

        if (request.getSchedules() != null) {
            for (BookingCreateRequest.ScheduleItem item : request.getSchedules()) {
                BookingSchedule schedule = BookingSchedule.builder()
                        .booking(booking)
                        .dayOfWeek(item.getDayOfWeek())
                        .startTime(item.getStartTime())
                        .endTime(item.getEndTime())
                        .build();
                booking.getSchedules().add(schedule);
            }
        }

        Booking savedBooking = bookingRepository.save(booking);

        List<Session> generatedSessions = new ArrayList<>();

        if (Boolean.TRUE.equals(request.getIsRecurring())) {
            LocalDate start = request.getRecurringStartDate();
            LocalDate end = request.getRecurringEndDate();
            if (start == null || end == null) {
                throw new IllegalArgumentException("Hợp đồng định kỳ yêu cầu phải có Ngày bắt đầu và Ngày kết thúc");
            }

            for (BookingSchedule sched : savedBooking.getSchedules()) {
                List<LocalDate> datesForThisSched = new ArrayList<>();
                LocalDate date = start;
                while (!date.isAfter(end)) {
                    if (date.getDayOfWeek().getValue() == sched.getDayOfWeek()) {
                        datesForThisSched.add(date);
                        Session session = Session.builder()
                                .booking(savedBooking)
                                .sessionDate(date)
                                .startTime(sched.getStartTime())
                                .endTime(sched.getEndTime())
                                .status(SessionStatus.PENDING)
                                .build();
                        generatedSessions.add(session);
                    }
                    date = date.plusDays(1);
                }
                if (!datesForThisSched.isEmpty()) {
                    boolean hasOverlap = sessionRepository.existsOverlappingSessions(
                            request.getTutorId(), datesForThisSched, sched.getStartTime(), sched.getEndTime());
                    if (hasOverlap) {
                        throw new IllegalStateException("Gia sư đã có lịch vào Thứ " + (sched.getDayOfWeek() == 7 ? "Chủ Nhật" : (sched.getDayOfWeek() + 1)) + " (" + sched.getStartTime() + " - " + sched.getEndTime() + "). Vui lòng chọn ca hoặc gia sư khác");
                    }
                }
            }
        } else {
            LocalDate singleDate = request.getRecurringStartDate();
            if (singleDate == null) {
                throw new IllegalArgumentException("Hợp đồng một buổi yêu cầu phải có Ngày bắt đầu (recurringStartDate)");
            }

            for (BookingSchedule sched : savedBooking.getSchedules()) {
                if (singleDate.getDayOfWeek().getValue() != sched.getDayOfWeek()) {
                    throw new IllegalArgumentException("Ngày bắt đầu không khớp với Thứ đã chọn trong lịch trình");
                }

                boolean hasOverlap = sessionRepository.existsOverlappingSessions(
                        request.getTutorId(), List.of(singleDate), sched.getStartTime(), sched.getEndTime());
                if (hasOverlap) {
                    throw new IllegalStateException("Gia sư đã có lịch vào lúc " + sched.getStartTime() + " - " + sched.getEndTime() + " ngày " + singleDate + ". Vui lòng chọn ca khác");
                }

                Session session = Session.builder()
                        .booking(savedBooking)
                        .sessionDate(singleDate)
                        .startTime(sched.getStartTime())
                        .endTime(sched.getEndTime())
                        .status(SessionStatus.PENDING)
                        .build();
                generatedSessions.add(session);
            }
        }

        List<Session> savedSessions = sessionRepository.saveAll(generatedSessions);

        List<SessionResponse> sessionResponses = savedSessions.stream().map(s -> SessionResponse.builder()
                .id(s.getId())
                .bookingId(s.getBooking().getId())
                .sessionDate(s.getSessionDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .status(s.getStatus())
                .build()).collect(Collectors.toList());

        return toBookingResponse(savedBooking, sessionResponses);
    }
    
    @Override
    public PageResponse<BookingResponse> getBookings(Long userId, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Booking> bookingPage;

        if ("ADMIN".equalsIgnoreCase(role)) {
            bookingPage = bookingRepository.findAll(pageable);
        } else if ("PARENT".equalsIgnoreCase(role)) {
            bookingPage = bookingRepository.findByParent_User_Id(userId, pageable);
        } else {
            bookingPage = bookingRepository.findByTutor_User_Id(userId, pageable);
        }

        List<BookingResponse> content = bookingPage.getContent().stream()
                .map(b -> toBookingResponse(b, null))
                .collect(Collectors.toList());

        return PageResponse.<BookingResponse>builder()
                .content(content)
                .page(bookingPage.getNumber())
                .size(bookingPage.getSize())
                .totalElements(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .last(bookingPage.isLast())
                .build();
    }

    @Override
    public BookingResponse getBookingById(Long userId, String role, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Hợp đồng này"));

        if (!"ADMIN".equalsIgnoreCase(role)) {
            boolean isOwner = false;
            if ("PARENT".equalsIgnoreCase(role)) {
                isOwner = booking.getParent().getUser().getId().equals(userId);
            } else if ("TUTOR".equalsIgnoreCase(role)) {
                isOwner = booking.getTutor().getUser().getId().equals(userId);
            }
            if (!isOwner) {
                throw new AccessDeniedException("Bạn không có quyền xem Hợp đồng này");
            }
        }

        List<SessionResponse> sessionResponses = sessionRepository.findByBookingId(bookingId)
                .stream().map(s -> SessionResponse.builder()
                        .id(s.getId())
                        .bookingId(s.getBooking().getId())
                        .sessionDate(s.getSessionDate())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .status(s.getStatus())
                        .build())
                .collect(Collectors.toList());

        return toBookingResponse(booking, sessionResponses);
    }

    @Override
    @Transactional
    public BookingResponse pauseBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Hợp đồng này"));

        if (!booking.getParent().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền Tạm dừng Hợp đồng này");
        }

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalStateException("Chỉ có thể Tạm dừng Hợp đồng đang Hoạt động (ACTIVE). Trạng thái hiện tại: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.PAUSED);
        bookingRepository.save(booking);

        List<Session> pendingSessions = sessionRepository.findByBookingIdAndStatus(bookingId, SessionStatus.PENDING);
        for (Session session : pendingSessions) {
            session.setStatus(SessionStatus.CANCELLED);
        }
        sessionRepository.saveAll(pendingSessions);

        emailService.sendBookingStatusChangedEmail(
                booking.getTutor().getUser().getEmail(),
                booking.getTutor().getUser().getFullName(),
                booking.getSubject().getName(),
                "PAUSED");

        return toBookingResponse(booking, null);
    }

    @Override
    @Transactional
    public BookingResponse resumeBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Hợp đồng này"));

        if (!booking.getParent().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền Tiếp tục Khóa học này");
        }

        if (booking.getStatus() != BookingStatus.PAUSED) {
            throw new IllegalStateException("Chỉ có thể Tiếp tục khóa học đang Bị Tạm Dừng (PAUSED). Trạng thái hiện tại: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.ACTIVE);
        bookingRepository.save(booking);

        List<Session> newSessions = new ArrayList<>();
        if (booking.getIsRecurring() && booking.getRecurringEndDate() != null) {
            LocalDate today = LocalDate.now();
            LocalDate startFrom = today.isAfter(booking.getRecurringStartDate()) ? today : booking.getRecurringStartDate();
            LocalDate end = booking.getRecurringEndDate();

            for (BookingSchedule sched : booking.getSchedules()) {
                LocalDate date = startFrom;
                while (!date.isAfter(end)) {
                    if (date.getDayOfWeek().getValue() == sched.getDayOfWeek()) {
                        Session session = Session.builder()
                                .booking(booking)
                                .sessionDate(date)
                                .startTime(sched.getStartTime())
                                .endTime(sched.getEndTime())
                                .status(SessionStatus.PENDING)
                                .build();
                        newSessions.add(session);
                    }
                    date = date.plusDays(1);
                }
            }
            sessionRepository.saveAll(newSessions);
        }

        emailService.sendBookingStatusChangedEmail(
                booking.getTutor().getUser().getEmail(),
                booking.getTutor().getUser().getFullName(),
                booking.getSubject().getName(),
                "ACTIVE");

        return toBookingResponse(booking, null);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long userId, String role, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Hợp đồng này"));

        boolean isParent = booking.getParent().getUser().getId().equals(userId);
        boolean isTutor = booking.getTutor().getUser().getId().equals(userId);
        if (!isParent && !isTutor) {
            throw new AccessDeniedException("Bạn không có quyền Hủy Hợp đồng này");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Hợp đồng này đã bị Hủy từ trước");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        List<Session> pendingSessions = sessionRepository.findByBookingIdAndStatus(bookingId, SessionStatus.PENDING);
        List<Session> confirmedSessions = sessionRepository.findByBookingIdAndStatus(bookingId, SessionStatus.CONFIRMED);

        for (Session s : pendingSessions) {
            s.setStatus(SessionStatus.CANCELLED);
        }
        for (Session s : confirmedSessions) {
            s.setStatus(SessionStatus.CANCELLED);
        }

        sessionRepository.saveAll(pendingSessions);
        sessionRepository.saveAll(confirmedSessions);

        // Bắn Email Báo Động cho Phía Bị Hủy (Nạn nhân còn lại)
        User receiver = isParent ? booking.getTutor().getUser() : booking.getParent().getUser();
        emailService.sendBookingStatusChangedEmail(
                receiver.getEmail(),
                receiver.getFullName(),
                booking.getSubject().getName(),
                "CANCELLED");

        return toBookingResponse(booking, null);
    }

    private BookingResponse toBookingResponse(Booking booking, List<SessionResponse> sessionResponses) {
        List<BookingResponse.ScheduleResponseItem> scheduleResponses = new ArrayList<>();
        if (booking.getSchedules() != null) {
            scheduleResponses = booking.getSchedules().stream()
                    .map(sched -> BookingResponse.ScheduleResponseItem.builder()
                            .id(sched.getId())
                            .dayOfWeek(sched.getDayOfWeek())
                            .startTime(sched.getStartTime())
                            .endTime(sched.getEndTime())
                            .build())
                    .collect(Collectors.toList());
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .parentId(booking.getParent().getId())
                .tutorId(booking.getTutor().getId())
                .subjectId(booking.getSubject().getId())
                .studentId(booking.getStudent() != null ? booking.getStudent().getId() : null)
                .gradeLevel(booking.getGradeLevel())
                .pricePerSession(booking.getPricePerSession())
                .teachingMode(booking.getTeachingMode())
                .isRecurring(booking.getIsRecurring())
                .schedules(scheduleResponses)
                .recurringStartDate(booking.getRecurringStartDate())
                .recurringEndDate(booking.getRecurringEndDate())
                .status(booking.getStatus())
                .sessions(sessionResponses)
                .build();
    }
}
