package org.tutorbooking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorbooking.domain.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByBookingId(Long bookingId);
    Page<Review> findByTutorId(Long tutorId, Pageable pageable);
}
