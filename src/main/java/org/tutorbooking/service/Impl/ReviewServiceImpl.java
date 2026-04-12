package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Booking;
import org.tutorbooking.domain.entity.Review;
import org.tutorbooking.domain.enums.BookingStatus;
import org.tutorbooking.dto.request.ReviewCreateRequest;
import org.tutorbooking.dto.response.PageResponse;
import org.tutorbooking.dto.response.ReviewResponse;
import org.tutorbooking.exception.ResourceNotFoundException;
import org.tutorbooking.repository.BookingRepository;
import org.tutorbooking.repository.ReviewRepository;
import org.tutorbooking.service.ReviewService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Lịch học/Booking này."));

        if (!booking.getParent().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền đánh giá! Chỉ phụ huynh đặt lịch này mới được phép đánh giá.");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Chỉ được phép đánh giá khi lịch học đã HOÀN THÀNH. Trạng thái hiện tại: " + booking.getStatus());
        }

        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new IllegalStateException("Lịch học này đã được bạn đánh giá rồi, không thể đánh giá lại.");
        }

        Review review = Review.builder()
                .booking(booking)
                .parent(booking.getParent())
                .tutor(booking.getTutor())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        return toResponse(review, booking);
    }

    @Override
    public PageResponse<ReviewResponse> getReviews(Long tutorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewPage;

        if (tutorId != null) {
            reviewPage = reviewRepository.findByTutorId(tutorId, pageable);
        } else {
            reviewPage = reviewRepository.findAll(pageable);
        }

        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(review -> toResponse(review, review.getBooking()))
                .collect(Collectors.toList());

        return PageResponse.<ReviewResponse>builder()
                .content(content)
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .build();
    }

    private ReviewResponse toResponse(Review review, Booking booking) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(booking.getId())
                .parentName(review.getParent().getUser().getFullName())
                .tutorName(review.getTutor().getUser().getFullName())
                .subjectName(booking.getSubject().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
