package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorbooking.domain.entity.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByParent_User_Id(Long userId);
    List<Booking> findByTutor_User_Id(Long userId);
}
