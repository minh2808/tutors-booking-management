package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.*;
import org.tutorbooking.domain.enums.BookingStatus;
import org.tutorbooking.domain.enums.SessionStatus;
import org.tutorbooking.dto.request.BookingCreateRequest;
import org.tutorbooking.dto.response.BookingResponse;
import org.tutorbooking.dto.response.SessionResponse;
import org.tutorbooking.exception.ResourceNotFoundException;
import org.tutorbooking.repository.*;
import org.tutorbooking.service.BookingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public BookingResponse createBooking(Long userId, BookingCreateRequest request) {

        Parent parent = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent profile not found for this user"));

        Tutor tutor = tutorRepository.findById(request.getTutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        Student student = null;
        if (request.getStudentId() != null) {
            student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        }

        TutorSubject tutorSubject = tutorSubjectRepository
                .findByTutorIdAndSubjectIdAndGradeLevel(request.getTutorId(), request.getSubjectId(), (int) request.getGradeLevel())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor does not teach this subject at this grade level"));

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
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .recurringStartDate(request.getRecurringStartDate())
                .recurringEndDate(request.getRecurringEndDate())
                .status(BookingStatus.ACTIVE)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        List<Session> generatedSessions = new ArrayList<>();

        if (Boolean.TRUE.equals(request.getIsRecurring())) {
            LocalDate start = request.getRecurringStartDate();
            LocalDate end = request.getRecurringEndDate();
            if (start == null || end == null || request.getDayOfWeek() == null) {
                throw new IllegalArgumentException("recurringStartDate, recurringEndDate, dayOfWeek are required for recurring booking");
            }

            LocalDate date = start;
            while (!date.isAfter(end)) {
                if (date.getDayOfWeek().getValue() == request.getDayOfWeek()) {
                    Session session = Session.builder()
                            .booking(savedBooking)
                            .sessionDate(date)
                            .startTime(request.getStartTime())
                            .endTime(request.getEndTime())
                            .status(SessionStatus.PENDING)
                            .build();
                    generatedSessions.add(session);
                }
                date = date.plusDays(1);
            }
        } else {
            LocalDate singleDate = request.getRecurringStartDate();
            if (singleDate == null) {
                throw new IllegalArgumentException("recurringStartDate is required for one-time booking");
            }
            Session session = Session.builder()
                    .booking(savedBooking)
                    .sessionDate(singleDate)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .status(SessionStatus.PENDING)
                    .build();
            generatedSessions.add(session);
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
    public List<BookingResponse> getMyBookings(Long userId, String role) {
        List<Booking> bookings;
        if ("PARENT".equalsIgnoreCase(role)) {
            bookings = bookingRepository.findByParent_User_Id(userId);
        } else {
            bookings = bookingRepository.findByTutor_User_Id(userId);
        }
        return bookings.stream().map(b -> toBookingResponse(b, null)).collect(Collectors.toList());
    }

    @Override
    public BookingResponse getBookingById(Long userId, String role, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!"ADMIN".equalsIgnoreCase(role)) {
            boolean isOwner = false;
            if ("PARENT".equalsIgnoreCase(role)) {
                isOwner = booking.getParent().getUser().getId().equals(userId);
            } else if ("TUTOR".equalsIgnoreCase(role)) {
                isOwner = booking.getTutor().getUser().getId().equals(userId);
            }
            if (!isOwner) {
                throw new org.springframework.security.access.AccessDeniedException("You don't have permission to view this booking");
            }
        }
        // Load danh sách sessions kèm theo booking
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
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(b -> toBookingResponse(b, null))
                .collect(Collectors.toList());
    }


    private BookingResponse toBookingResponse(Booking booking, List<SessionResponse> sessionResponses) {
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
                .dayOfWeek(booking.getDayOfWeek())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .recurringStartDate(booking.getRecurringStartDate())
                .recurringEndDate(booking.getRecurringEndDate())
                .status(booking.getStatus())
                .sessions(sessionResponses)
                .build();
    }
}

