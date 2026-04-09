package org.tutorbooking.service.Impl;

import org.tutorbooking.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.tutorbooking.service.TutorService;
import org.tutorbooking.repository.TutorRepository;
import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.Review;
import org.tutorbooking.dto.request.UpdateTutorRequest;
import org.tutorbooking.dto.response.ReviewResponse;
import org.tutorbooking.dto.response.TutorDetailResponse;
import org.tutorbooking.dto.response.TutorReviewSummaryResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.tutorbooking.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public TutorDetailResponse getTutorDetail(Long tutorId) {
        Tutor tutor = tutorRepository.findDetailById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ gia sư: " + tutorId));

        if (!"approved".equals(tutor.getApprovalStatus())) {
            throw new ResourceNotFoundException("Hồ sơ gia sư này chưa được phê duyệt công khai.");
        }

        return mapToTutorDetailResponse(tutor);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateTutorRequest req) {
        Tutor tutor = tutorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ gia sư để cập nhật."));

        tutor.setEducationLevel(req.getEducationLevel());
        tutor.setExperience(req.getExperience());
        tutor.setQualifications(req.getQualifications());
        tutor.setTeachingMode(req.getTeachingMode());
        tutor.setTeachingArea(req.getTeachingArea());
    }

    @Override
    public Page<TutorDetailResponse> searchTutors(Long subjectId, Integer grade, Long minPrice, Long maxPrice, String teachingMode, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Tutor> tutors = tutorRepository.searchApprovedTutors(subjectId, grade, minPrice, maxPrice, teachingMode, pageable);
        
        return tutors.map(this::mapToTutorDetailResponse);
    }

    
    @Override
    public TutorReviewSummaryResponse getTutorReviews(Long tutorId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Double avgRating = reviewRepository.getAverageRatingByTutorId(tutorId);
        if (avgRating == null) avgRating = 0.0;
        
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

    /**
     * Map Tutor entity to TutorDetailResponse DTO consistently.
     */
    private TutorDetailResponse mapToTutorDetailResponse(Tutor tutor) {
        TutorDetailResponse res = new TutorDetailResponse();
        res.setId(tutor.getId());

        // Map User Info
        if (tutor.getUser() != null) {
            res.setFullName(tutor.getUser().getFullName());
            res.setAvatarUrl(tutor.getUser().getAvatarUrl());
            res.setEmail(tutor.getUser().getEmail());
        }

        // Map Tutor Info
        res.setEducationLevel(tutor.getEducationLevel());
        res.setExperience(tutor.getExperience());
        res.setQualifications(tutor.getQualifications());
        res.setTeachingMode(tutor.getTeachingMode());
        res.setTeachingArea(tutor.getTeachingArea());
        res.setApprovalStatus(tutor.getApprovalStatus());

        return res;
    }
    // =========================================
    // CỤM CHỨC NĂNG DÀNH CHO ADMIN
    // =========================================
    @Override
    public Page<TutorDetailResponse> getPendingTutors (int page, int size) {
        // Lấy danh sách chờ duyệt, sắp xếp người đăng ký cũ lên trước (để duyệt trước)
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Tutor> tutors = tutorRepository.findPendingTutors(pageable);

        return tutors.map(tutor -> {
            TutorDetailResponse dto = new TutorDetailResponse();
            dto.setId(tutor.getId());
            dto.setFullName(tutor.getUser().getFullName());
            dto.setAvatarUrl(tutor.getUser().getAvatarUrl());
            dto.setEmail(tutor.getUser().getEmail());
            dto.setEducationLevel(tutor.getEducationLevel());
            dto.setExperience(tutor.getExperience());
            dto.setQualifications(tutor.getQualifications());
            dto.setTeachingMode(tutor.getTeachingMode());
            dto.setTeachingArea(tutor.getTeachingArea());
            dto.setApprovalStatus(tutor.getApprovalStatus());
            return dto;
        });
    }

    @Override
    @Transactional
    public void approveTutor(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ gia sư"));
        
        if (!"pending".equals(tutor.getApprovalStatus())) {
            throw new RuntimeException("Hồ sơ này không ở trạng thái chờ duyệt!");
        }
        
        tutor.setApprovalStatus("approved");
        tutor.setRejectionReason(null); // Xóa lý do từ chối cũ (nếu có)
    }

    @Override
    @Transactional
    public void rejectTutor(Long tutorId, String reason) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ gia sư"));
        
        if (!"pending".equals(tutor.getApprovalStatus())) {
            throw new RuntimeException("Hồ sơ này không ở trạng thái chờ duyệt!");
        }
        
        tutor.setApprovalStatus("rejected");
        tutor.setRejectionReason(reason);
    }
}