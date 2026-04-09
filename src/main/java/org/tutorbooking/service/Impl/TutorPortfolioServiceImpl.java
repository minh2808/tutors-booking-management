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
                .orElseThrow(() -> new RuntimeException("Tutor not found for user ID: " + userId));
    }

    private Tutor getTutorById(Long tutorId) {
        return tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found with ID: " + tutorId));
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
        // verify tutor exists
        getTutorById(tutorId);
        
        List<TutorSubject> subjects = tutorSubjectRepository.findByTutorIdWithSubject(tutorId);
        return subjects.stream().map(this::mapToTutorSubjectResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TutorSubjectResponse addTutorSubject(Long userId, TutorSubjectRequest request) {
        Tutor tutor = getTutorByUserId(userId);
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (tutorSubjectRepository.findByTutorIdAndSubjectIdAndGradeLevel(tutor.getId(), subject.getId(), request.getGradeLevel()).isPresent()) {
            throw new RuntimeException("This subject and grade level is already registered by the tutor");
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
                .orElseThrow(() -> new RuntimeException("Tutor subject entry not found"));

        if (!tutorSubject.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("You do not have permission to modify this entry");
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
                .orElseThrow(() -> new RuntimeException("Tutor subject entry not found"));

        if (!tutorSubject.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("You do not have permission to delete this entry");
        }

        tutorSubjectRepository.delete(tutorSubject);
    }

    // --- Tutor Availability ---

    @Override
    @Transactional(readOnly = true)
    public List<TutorAvailabilityResponse> getTutorAvailability(Long tutorId) {
        getTutorById(tutorId);
        List<TutorAvailability> availabilities = tutorAvailabilityRepository.findByTutorIdAndIsActiveTrue(tutorId);
        
        return availabilities.stream().map(this::mapToTutorAvailabilityResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TutorAvailabilityResponse addTutorAvailability(Long userId, TutorAvailabilityRequest request) {
        Tutor tutor = getTutorByUserId(userId);

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
                .orElseThrow(() -> new RuntimeException("Availability entry not found"));

        if (!availability.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("You do not have permission to modify this entry");
        }

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
                .orElseThrow(() -> new RuntimeException("Availability entry not found"));

        if (!availability.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("You do not have permission to delete this entry");
        }

        tutorAvailabilityRepository.delete(availability);
    }
}
