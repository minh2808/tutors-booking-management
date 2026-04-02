package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorbooking.domain.entity.Session;
import org.tutorbooking.domain.enums.SessionStatus;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByBookingId(Long bookingId);
    List<Session> findByBookingIdAndStatus(Long bookingId, SessionStatus status);
}
