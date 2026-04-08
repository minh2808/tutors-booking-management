
package org.tutorbooking.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.tutorbooking.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.tutorbooking.service.TutorService;
import org.tutorbooking.repository.TutorRepository;
import org.tutorbooking.repository.TutorSubjectRepository;
import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.TutorSubject;
import org.tutorbooking.domain.entity.Review;
import org.tutorbooking.domain.entity.Subject;
import org.tutorbooking.dto.request.UpdateTutorRequest;
import org.tutorbooking.dto.response.ReviewResponse;
import org.tutorbooking.dto.response.TutorDetailResponse;
import org.tutorbooking.dto.response.TutorReviewSummaryResponse;
import org.tutorbooking.dto.request.SubjectRequest;
import org.tutorbooking.repository.ParentRepository;

import java.util.List;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.tutorbooking.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final TutorSubjectRepository tutorSubjectRepository;
    private final ReviewRepository reviewRepository;
    @Override
    public TutorDetailResponse getTutorDetail(Long tutorId) {
        Tutor tutor = tutorRepository.findDetailById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gia sư với id: " + tutorId));

        if (!"approved".equals(tutor.getApprovalStatus())) {
            throw new ResourceNotFoundException("Hồ sơ gia sư chưa được duyệt");
        }

        TutorDetailResponse res = new TutorDetailResponse();

        res.setId(tutor.getId());

        res.setFullName(tutor.getUser().getFullName());
        res.setAvatarUrl(tutor.getUser().getAvatarUrl());
        res.setEmail(tutor.getUser().getEmail());

        res.setEducationLevel(tutor.getEducationLevel());
        res.setExperience(tutor.getExperience());
        res.setQualifications(tutor.getQualifications());
        res.setTeachingMode(tutor.getTeachingMode());
        res.setTeachingArea(tutor.getTeachingArea());
        res.setApprovalStatus(tutor.getApprovalStatus());

        return res;
    }


    @Override
    public Tutor getMyProfile(Long userId) {
        return tutorRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ gia sư"));
    }


    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateTutorRequest req) {

        Tutor tutor = tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ gia sư"));

        tutor.setEducationLevel(req.getEducationLevel());
        tutor.setExperience(req.getExperience());
        tutor.setQualifications(req.getQualifications());
        tutor.setTeachingMode(req.getTeachingMode());
        tutor.setTeachingArea(req.getTeachingArea());
    }


    @Override
    @Transactional
    public void updateSubjects(Long userId, List<SubjectRequest> reqs) {

        Tutor tutor = tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ gia sư"));

        tutorSubjectRepository.deleteByTutorId(tutor.getId());

        List<TutorSubject> list = new ArrayList<>();

        for (SubjectRequest r : reqs) {

            TutorSubject ts = new TutorSubject();
            ts.setTutor(tutor);

            Subject subject = new Subject();
            subject.setId(r.getSubjectId());

            ts.setSubject(subject);
            ts.setGradeLevel(r.getGradeLevel());
            ts.setPricePerSession(r.getPricePerSession());

            list.add(ts);
        }

        tutorSubjectRepository.saveAll(list);
    }


    @Override
    public List<TutorSubject> getSubjects(Long tutorId) {
        return tutorSubjectRepository.findByTutorIdWithSubject(tutorId);
    }

    @Override
    public Page<TutorDetailResponse> searchTutors(Long subjectId, Integer grade, Long minPrice, Long maxPrice, String teachingMode, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Tutor> tutors = tutorRepository.searchApprovedTutors(subjectId, grade, minPrice, maxPrice, teachingMode, pageable);
        
        return tutors.map(tutor -> {
            TutorDetailResponse dto = new TutorDetailResponse();
            dto.setId(tutor.getId());
            dto.setFullName(tutor.getUser().getFullName());
            dto.setAvatarUrl(tutor.getUser().getAvatarUrl());
            dto.setTeachingMode(tutor.getTeachingMode());
            dto.setTeachingArea(tutor.getTeachingArea());
            return dto;
        });
    }

    // =========================================
    // LẤY ĐÁNH GIÁ (REVIEW) CHUẨN VỚI DTO CỦA BẠN
    // =========================================
    @Override
    public TutorReviewSummaryResponse getTutorReviews(Long tutorId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Double avgRating = reviewRepository.getAverageRatingByTutorId(tutorId);
        long totalReviews = reviewRepository.countByTutorId(tutorId);
        
        Page<Review> reviewPage = reviewRepository.findByTutorId(tutorId, pageable);
        
        Page<ReviewResponse> reviewDtos = reviewPage.map(review -> ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId()) 
                .parentName(review.getParent().getUser().getFullName())
                .tutorName(review.getTutor().getUser().getFullName()) 
                .subjectName(review.getBooking().getSubject().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build());

        return TutorReviewSummaryResponse.builder()
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .totalReviews(totalReviews)
                .reviews(reviewDtos)
                .build();
    }
}