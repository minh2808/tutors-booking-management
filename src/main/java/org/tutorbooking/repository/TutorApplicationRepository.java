package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.tutorbooking.domain.entity.TutorApplication;
import org.tutorbooking.domain.enums.TutorApplicationStatus;

import java.util.List;
import java.util.Optional;

public interface TutorApplicationRepository extends JpaRepository<TutorApplication, Long> {

    // Lấy danh sách ứng tuyển của 1 yêu cầu (có tutor và subject)
    @Query("""
                SELECT ta FROM TutorApplication ta
                JOIN FETCH ta.tutor t
                JOIN FETCH t.user
                JOIN FETCH ta.request tr
                JOIN FETCH tr.subject
                WHERE ta.request.id = :requestId
                ORDER BY ta.createdAt DESC
            """)
    List<TutorApplication> findByRequestIdWithTutor(@Param("requestId") Long requestId);

    // Lấy danh sách ứng tuyển của tutor (có request và subject)
    @Query("""
                SELECT ta FROM TutorApplication ta
                JOIN FETCH ta.request tr
                JOIN FETCH tr.subject
                JOIN FETCH tr.parent p
                JOIN FETCH p.user
                WHERE ta.tutor.id = :tutorId
                ORDER BY ta.createdAt DESC
            """)
    List<TutorApplication> findByTutorIdWithRequest(@Param("tutorId") Long tutorId);

    // Kiểm tra tutor đã ứng tuyển chưa
    boolean existsByRequestIdAndTutorId(Long requestId, Long tutorId);

    // Lấy ứng tuyển cụ thể
    Optional<TutorApplication> findByRequestIdAndTutorId(Long requestId, Long tutorId);

    // Lấy danh sách ứng tuyển của tutor theo status
    List<TutorApplication> findByTutorIdAndStatus(Long tutorId, TutorApplicationStatus status);

    // Lấy danh sách ứng tuyển của yêu cầu theo status
    List<TutorApplication> findByRequestIdAndStatus(Long requestId, TutorApplicationStatus status);

    // Đếm số ứng tuyển cho 1 yêu cầu
    long countByRequestId(Long requestId);

    // Đếm số ứng tuyển chưa được phản hồi
    long countByRequestIdAndStatus(Long requestId, TutorApplicationStatus status);
}
