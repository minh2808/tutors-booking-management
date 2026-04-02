package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorbooking.domain.entity.Session;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByBookingId(Long bookingId);
}
