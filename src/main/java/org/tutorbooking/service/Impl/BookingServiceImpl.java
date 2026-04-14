package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.*;
import org.tutorbooking.domain.enums.BookingStatus;
import org.tutorbooking.domain.enums.PaymentMethod;
import org.tutorbooking.domain.enums.PaymentStatus;
import org.tutorbooking.domain.enums.PaymentType;
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
    private final PaymentRepository paymentRepository;

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
                .status(BookingStatus.WAITING_TUTOR_CONFIRM) // <-- OPTION 2: Chưa sinh Session, chỉ chờ Gia sư CONFIRM
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

        // KIỂM TRA ĐÈ LỊCH TRƯỚC (DỰ ĐOÁN)
        validateSchedulesOverlap(booking);

        Booking savedBooking = bookingRepository.save(booking);

        // Bắn email báo gia sư
        emailService.sendBookingStatusChangedEmail(
                tutor.getUser().getEmail(),
                tutor.getUser().getFullName(),
                subject.getName(),
                "WAITING_TUTOR_CONFIRM"
        );

        return toBookingResponse(savedBooking, new ArrayList<>());
    }
    
    // GIA SƯ BẤM ĐỒNG Ý -> TẠO HÓA ĐƠN KÉP
    @Transactional
    public BookingResponse acceptBookingByTutor(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Hợp đồng"));

        if (!booking.getTutor().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Chỉ gia sư của hợp đồng này mới có quyền Xác nhận");
        }

        if (booking.getStatus() != BookingStatus.WAITING_TUTOR_CONFIRM) {
            throw new IllegalStateException("Hợp đồng này không ở trạng thái chờ xác nhận");
        }

        // 1. Chuyển sang chờ thanh toán
        booking.setStatus(BookingStatus.PENDING_PAYMENTS);
        bookingRepository.save(booking);

        // 2. Tạo hóa đơn 10k cho Phụ huynh (Phí Môi Giới)
        Payment parentPayment = Payment.builder()
                .user(booking.getParent().getUser())
                .booking(booking)
                .amount(new BigDecimal("10000")) // FIXED 10k Nhu Sếp Yêu Cầu
                .paymentType(PaymentType.CLASS_FINDING_FEE)
                .paymentMethod(PaymentMethod.BANK_TRANSFER) // Dai dien cho PayOS VietQR
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(parentPayment);

        // Đã LƯỢC BỎ: Không tạo Hóa đơn cho Gia sư nữa để giảm phức tạp (Chỉ Phụ Huynh thanh toán)

        // Bắn Email báo nộp tiền cho Phụ Huynh
        emailService.sendBookingStatusChangedEmail(
                booking.getParent().getUser().getEmail(),
                booking.getParent().getUser().getFullName(),
                booking.getSubject().getName(),
                "PENDING_PAYMENTS"
        );

        return toBookingResponse(booking, new ArrayList<>());
    }

    // WEBHOOK SẼ GỌI HÀM NÀY KHI ĐỦ 2 HÓA ĐƠN COMPLETED
    @Transactional
    public void activateBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.ACTIVE);
        bookingRepository.save(booking);
        
        List<Session> newSessions = generateSessionsForBooking(booking);
        sessionRepository.saveAll(newSessions);
        
        emailService.sendBookingStatusChangedEmail(
                booking.getTutor().getUser().getEmail(),
                booking.getTutor().getUser().getFullName(),
                booking.getSubject().getName(),
                "ACTIVE"
        );
    }

    private void validateSchedulesOverlap(Booking booking) {
        if (Boolean.TRUE.equals(booking.getIsRecurring())) {
            LocalDate start = booking.getRecurringStartDate();
            LocalDate end = booking.getRecurringEndDate();
            if (start == null || end == null) {
                throw new IllegalArgumentException("Hợp đồng định kỳ yêu cầu Ngày bắt đầu và kết thúc");
            }
            for (BookingSchedule sched : booking.getSchedules()) {
                List<LocalDate> datesForThisSched = new ArrayList<>();
                LocalDate date = start;
                while (!date.isAfter(end)) {
                    if (date.getDayOfWeek().getValue() == sched.getDayOfWeek()) {
                        datesForThisSched.add(date);
                    }
                    date = date.plusDays(1);
                }
                if (!datesForThisSched.isEmpty()) {
                    boolean hasOverlap = sessionRepository.existsOverlappingSessions(
                            booking.getTutor().getId(), datesForThisSched, sched.getStartTime(), sched.getEndTime());
                    if (hasOverlap) {
                        throw new IllegalStateException("Gia sư đã có lịch vào Thứ " + (sched.getDayOfWeek() == 7 ? "Chủ Nhật" : (sched.getDayOfWeek() + 1)) + " (" + sched.getStartTime() + " - " + sched.getEndTime() + "). Vui lòng chọn ca hoặc gia sư khác");
                    }
                }
            }
        } else {
            LocalDate singleDate = booking.getRecurringStartDate();
            if (singleDate == null) {
                throw new IllegalArgumentException("Hợp đồng 1 buổi yêu cầu Ngày bắt đầu");
            }
            for (BookingSchedule sched : booking.getSchedules()) {
                if (singleDate.getDayOfWeek().getValue() != sched.getDayOfWeek()) {
                    throw new IllegalArgumentException("Ngày bắt đầu không khớp Thứ trong lịch trình");
                }
                boolean hasOverlap = sessionRepository.existsOverlappingSessions(
                        booking.getTutor().getId(), List.of(singleDate), sched.getStartTime(), sched.getEndTime());
                if (hasOverlap) {
                    throw new IllegalStateException("Gia sư đã có lịch vào " + sched.getStartTime() + " " + singleDate + ". Vui lòng chọn ca khác");
                }
            }
        }
    }

    private List<Session> generateSessionsForBooking(Booking booking) {
        List<Session> generatedSessions = new ArrayList<>();
        if (Boolean.TRUE.equals(booking.getIsRecurring())) {
            LocalDate start = booking.getRecurringStartDate();
            LocalDate end = booking.getRecurringEndDate();
            for (BookingSchedule sched : booking.getSchedules()) {
                LocalDate date = start;
                while (!date.isAfter(end)) {
                    if (date.getDayOfWeek().getValue() == sched.getDayOfWeek()) {
                        generatedSessions.add(Session.builder()
                                .booking(booking)
                                .sessionDate(date)
                                .startTime(sched.getStartTime())
                                .endTime(sched.getEndTime())
                                .status(SessionStatus.PENDING)
                                .build());
                    }
                    date = date.plusDays(1);
                }
            }
        } else {
            LocalDate singleDate = booking.getRecurringStartDate();
            for (BookingSchedule sched : booking.getSchedules()) {
                generatedSessions.add(Session.builder()
                        .booking(booking)
                        .sessionDate(singleDate)
                        .startTime(sched.getStartTime())
                        .endTime(sched.getEndTime())
                        .status(SessionStatus.PENDING)
                        .build());
            }
        }
        return generatedSessions;
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
                .parentName(booking.getParent().getUser().getFullName())
                .tutorId(booking.getTutor().getId())
                .tutorName(booking.getTutor().getUser().getFullName())
                .subjectId(booking.getSubject().getId())
                .studentId(booking.getStudent() != null ? booking.getStudent().getId() : null)
                .studentName(booking.getStudent() != null ? booking.getStudent().getFullName() : null)
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
