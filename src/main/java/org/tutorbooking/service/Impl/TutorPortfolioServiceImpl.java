package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Subject;
import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.TutorAvailability;
import org.tutorbooking.domain.entity.TutorSubject;
import org.tutorbooking.dto.request.TutorAvailabilityRequest;
import org.tutorbooking.dto.request.TutorSubjectRequest;
import org.tutorbooking.dto.response.TutorAvailabilityResponse;
import org.tutorbooking.dto.response.TutorSubjectResponse;
import org.tutorbooking.repository.SubjectRepository;
import org.tutorbooking.repository.TutorAvailabilityRepository;
import org.tutorbooking.repository.TutorRepository;
import org.tutorbooking.repository.TutorSubjectRepository;
import org.tutorbooking.service.TutorPortfolioService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TutorPortfolioServiceImpl implements TutorPortfolioService {

    private final TutorRepository tutorRepository;
    private final SubjectRepository subjectRepository;
    private final TutorSubjectRepository tutorSubjectRepository;
    private final TutorAvailabilityRepository tutorAvailabilityRepository;

    private Tutor getTutorByUserId(Long userId) {
        return tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ gia sư cho User ID: " + userId));
    }

    private Tutor getTutorById(Long tutorId) {
        return tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ gia sư với ID: " + tutorId));
    }

    private Tutor getApprovedTutorById(Long tutorId) {
        Tutor tutor = getTutorById(tutorId);
        if (!"approved".equals(tutor.getApprovalStatus())) {
            throw new RuntimeException("Hồ sơ gia sư này chưa được phê duyệt công khai");
        }
        return tutor;
    }

    private void validateTimeOverlap(Long tutorId, Integer dayOfWeek, java.time.LocalTime startTime, java.time.LocalTime endTime, Long excludeId) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new RuntimeException("Khung giờ không hợp lệ (Start time phải diễn ra trước End time)");
        }
        
        List<TutorAvailability> existing = tutorAvailabilityRepository.findByTutorIdAndIsActiveTrue(tutorId)
                .stream().filter(a -> a.getDayOfWeek().equals(dayOfWeek) 
                        && (excludeId == null || !a.getId().equals(excludeId)))
                .collect(Collectors.toList());

        for (TutorAvailability a : existing) {
            if (startTime.isBefore(a.getEndTime()) && endTime.isAfter(a.getStartTime())) {
                throw new RuntimeException(String.format("Lịch bị trùng với khung giờ đã đăng ký trước đó (%s - %s)", a.getStartTime(), a.getEndTime()));
            }
        }
    }

    private TutorSubjectResponse mapToTutorSubjectResponse(TutorSubject tutorSubject) {
        return TutorSubjectResponse.builder()
                .id(tutorSubject.getId())
                .subjectId(tutorSubject.getSubject().getId())
                .subjectName(tutorSubject.getSubject().getName())
                .gradeLevel(tutorSubject.getGradeLevel())
                .pricePerSession(tutorSubject.getPricePerSession())
                .build();
    }

    private TutorAvailabilityResponse mapToTutorAvailabilityResponse(TutorAvailability availability) {
        return TutorAvailabilityResponse.builder()
                .id(availability.getId())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .isActive(availability.getIsActive())
                .build();
    }

    // --- Tutor Subject ---

    @Override
    @Transactional(readOnly = true)
    public List<TutorSubjectResponse> getTutorSubjects(Long tutorId) {
        // verify tutor exists and is approved
        getApprovedTutorById(tutorId);
        
        List<TutorSubject> subjects = tutorSubjectRepository.findByTutorIdWithSubject(tutorId);
        return subjects.stream().map(this::mapToTutorSubjectResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TutorSubjectResponse addTutorSubject(Long userId, TutorSubjectRequest request) {
        Tutor tutor = getTutorByUserId(userId);
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học này trên hệ thống"));

        if (tutorSubjectRepository.findByTutorIdAndSubjectIdAndGradeLevel(tutor.getId(), subject.getId(), request.getGradeLevel()).isPresent()) {
            throw new RuntimeException("Môn học và khối lớp này đã được bạn đăng ký trước đó");
        }

        TutorSubject tutorSubject = TutorSubject.builder()
                .tutor(tutor)
                .subject(subject)
                .gradeLevel(request.getGradeLevel())
                .pricePerSession(request.getPricePerSession())
                .build();

        tutorSubject = tutorSubjectRepository.save(tutorSubject);

        return mapToTutorSubjectResponse(tutorSubject);
    }

    @Override
    @Transactional
    public TutorSubjectResponse updateTutorSubject(Long userId, Long tutorSubjectId, TutorSubjectRequest request) {
        Tutor tutor = getTutorByUserId(userId);
        TutorSubject tutorSubject = tutorSubjectRepository.findById(tutorSubjectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu đăng ký môn học"));

        if (!tutorSubject.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa mục chuyên môn này");
        }

        if (!tutorSubject.getGradeLevel().equals(request.getGradeLevel())) {
            boolean exists = tutorSubjectRepository.findByTutorIdAndSubjectIdAndGradeLevel(
                    tutor.getId(), 
                    tutorSubject.getSubject().getId(), 
                    request.getGradeLevel()
            ).isPresent();
            if (exists) {
                throw new RuntimeException("Môn học và khối lớp này đã được bạn đăng ký trước đó");
            }
        }

        tutorSubject.setGradeLevel(request.getGradeLevel());
        tutorSubject.setPricePerSession(request.getPricePerSession());

        tutorSubject = tutorSubjectRepository.save(tutorSubject);

        return mapToTutorSubjectResponse(tutorSubject);
    }

    @Override
    @Transactional
    public void removeTutorSubject(Long userId, Long tutorSubjectId) {
        Tutor tutor = getTutorByUserId(userId);
        TutorSubject tutorSubject = tutorSubjectRepository.findById(tutorSubjectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu đăng ký môn học"));

        if (!tutorSubject.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa mục chuyên môn này");
        }

        tutorSubjectRepository.delete(tutorSubject);
    }

    // --- Tutor Availability ---

    @Override
    @Transactional(readOnly = true)
    public List<TutorAvailabilityResponse> getTutorAvailability(Long tutorId) {
        getApprovedTutorById(tutorId);
        List<TutorAvailability> availabilities = tutorAvailabilityRepository.findByTutorIdAndIsActiveTrue(tutorId);
        
        return availabilities.stream().map(this::mapToTutorAvailabilityResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TutorAvailabilityResponse addTutorAvailability(Long userId, TutorAvailabilityRequest request) {
        Tutor tutor = getTutorByUserId(userId);
        
        validateTimeOverlap(tutor.getId(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), null);

        TutorAvailability availability = TutorAvailability.builder()
                .tutor(tutor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        availability = tutorAvailabilityRepository.save(availability);

        return mapToTutorAvailabilityResponse(availability);
    }

    @Override
    @Transactional
    public TutorAvailabilityResponse updateTutorAvailability(Long userId, Long availabilityId, TutorAvailabilityRequest request) {
        Tutor tutor = getTutorByUserId(userId);
        TutorAvailability availability = tutorAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu khung giờ rảnh"));

        if (!availability.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa khung giờ này");
        }

        validateTimeOverlap(tutor.getId(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), availability.getId());

        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        if (request.getIsActive() != null) {
            availability.setIsActive(request.getIsActive());
        }

        availability = tutorAvailabilityRepository.save(availability);

        return mapToTutorAvailabilityResponse(availability);
    }

    @Override
    @Transactional
    public void removeTutorAvailability(Long userId, Long availabilityId) {
        Tutor tutor = getTutorByUserId(userId);
        TutorAvailability availability = tutorAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu khung giờ rảnh"));

        if (!availability.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa khung giờ này");
        }

        tutorAvailabilityRepository.delete(availability);
    }
}
