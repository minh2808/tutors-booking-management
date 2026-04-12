package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tutorbooking.domain.entity.Payment;
import org.tutorbooking.domain.enums.PaymentStatus;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);
    List<Payment> findByBookingIdAndStatus(Long bookingId, PaymentStatus status);
}
