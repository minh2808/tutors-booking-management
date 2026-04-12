package org.tutorbooking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tutorbooking.domain.entity.Session;
import org.tutorbooking.domain.enums.SessionStatus;

import java.time.LocalDate;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByBookingId(Long bookingId);
    List<Session> findByBookingIdAndStatus(Long bookingId, SessionStatus status);

    // Lọc sessions theo userId (qua booking → parent/tutor), có filter ngày và status
    @Query("SELECT s FROM Session s WHERE " +
            "(s.booking.parent.user.id = :userId OR s.booking.tutor.user.id = :userId) " +
            "AND (:startDate IS NULL OR s.sessionDate >= :startDate) " +
            "AND (:endDate IS NULL OR s.sessionDate <= :endDate) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "ORDER BY s.sessionDate ASC, s.startTime ASC")
    Page<Session> findSessionsByFilters(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") SessionStatus status,
            Pageable pageable);

    // ADMIN xem tất cả sessions
    @Query("SELECT s FROM Session s WHERE " +
            "(:startDate IS NULL OR s.sessionDate >= :startDate) " +
            "AND (:endDate IS NULL OR s.sessionDate <= :endDate) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "ORDER BY s.sessionDate ASC, s.startTime ASC")
    Page<Session> findAllSessionsByFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") SessionStatus status,
            Pageable pageable);

    // Kiểm tra chống trùng lịch (Chỉ xét các buổi học thật sự được lên lịch: PENDING, CONFIRMED)
    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.booking.tutor.user.id = :tutorUserId " +
           "AND s.sessionDate IN :dates " +
           "AND s.status IN (org.tutorbooking.domain.enums.SessionStatus.PENDING, org.tutorbooking.domain.enums.SessionStatus.CONFIRMED) " +
           "AND (s.startTime < :endTime AND s.endTime > :startTime)")
    boolean existsOverlappingSessions(
            @Param("tutorUserId") Long tutorUserId,
            @Param("dates") List<LocalDate> dates,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime);
}
