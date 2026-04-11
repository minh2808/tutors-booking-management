package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.tutorbooking.domain.entity.TutorRequest;
import org.tutorbooking.domain.enums.TutorRequestStatus;

import java.util.List;
import java.util.Optional;

public interface TutorRequestRepository extends JpaRepository<TutorRequest, Long> {

    // Lấy danh sách yêu cầu của parent (có subject)
    @Query("""
                SELECT tr FROM TutorRequest tr
                JOIN FETCH tr.subject
                WHERE tr.parent.id = :parentId
                ORDER BY tr.createdAt DESC
            """)
    List<TutorRequest> findByParentIdWithSubject(@Param("parentId") Long parentId);

    // Lấy chi tiết 1 yêu cầu (có subject và parent user)
    @Query("""
                SELECT tr FROM TutorRequest tr
                JOIN FETCH tr.subject
                JOIN FETCH tr.parent p
                JOIN FETCH p.user
                WHERE tr.id = :id
            """)
    Optional<TutorRequest> findDetailById(@Param("id") Long id);

    // Lấy danh sách yêu cầu theo danh sách status
    @Query("""
            SELECT tr FROM TutorRequest tr
            JOIN FETCH tr.subject
            JOIN FETCH tr.parent p
            JOIN FETCH p.user
            WHERE tr.status IN :statuses
            ORDER BY tr.createdAt DESC
            """)
    List<TutorRequest> findByStatusIn(@Param("statuses") List<TutorRequestStatus> statuses);

    // Lấy danh sách yêu cầu theo status
    List<TutorRequest> findByStatus(TutorRequestStatus status);

    // Lấy danh sách yêu cầu của parent theo status
    List<TutorRequest> findByParentIdAndStatus(Long parentId, TutorRequestStatus status);

    // Đếm số yêu cầu theo status
    long countByStatus(TutorRequestStatus status);
}
