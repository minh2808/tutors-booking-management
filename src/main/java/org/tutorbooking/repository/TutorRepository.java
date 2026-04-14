package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.tutorbooking.domain.entity.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.tutorbooking.dto.response.TopTutorResponse;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByUserId(Long userId);

    // Lấy thông tin chi tiết gia sư (bao gồm User)
    @Query("""
                SELECT t FROM Tutor t
                JOIN FETCH t.user
                WHERE t.id = :id
            """)
    Optional<Tutor> findDetailById(@Param("id") Long id);

    boolean existsByUserId(Long userId);

    @Query(value = """
                SELECT DISTINCT t FROM Tutor t
                JOIN FETCH t.user
                LEFT JOIN TutorSubject ts ON ts.tutor = t
                WHERE t.approvalStatus = 'approved'
                AND (:subjectId IS NULL OR ts.subject.id = :subjectId)
                AND (:grade IS NULL OR ts.gradeLevel = :grade)
                AND (:minPrice IS NULL OR ts.pricePerSession >= :minPrice)
                AND (:maxPrice IS NULL OR ts.pricePerSession <= :maxPrice)
                AND (:teachingMode IS NULL OR :teachingMode = '' OR t.teachingMode = :teachingMode)
            """,
            countQuery = """
                SELECT COUNT(DISTINCT t) FROM Tutor t
                LEFT JOIN TutorSubject ts ON ts.tutor = t
                WHERE t.approvalStatus = 'approved'
                AND (:subjectId IS NULL OR ts.subject.id = :subjectId)
                AND (:grade IS NULL OR ts.gradeLevel = :grade)
                AND (:minPrice IS NULL OR ts.pricePerSession >= :minPrice)
                AND (:maxPrice IS NULL OR ts.pricePerSession <= :maxPrice)
                AND (:teachingMode IS NULL OR :teachingMode = '' OR t.teachingMode = :teachingMode)
            """)
    Page<Tutor> searchApprovedTutors(
            @Param("subjectId") Long subjectId,
            @Param("grade") Integer grade,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("teachingMode") String teachingMode,
            Pageable pageable
    );

    @Query(value = """
                SELECT t FROM Tutor t
                JOIN FETCH t.user
                WHERE t.approvalStatus = 'pending'
            """,
            countQuery = """
                SELECT COUNT(t) FROM Tutor t
                WHERE t.approvalStatus = 'pending'
            """)
    Page<Tutor> findPendingTutors(Pageable pageable);

    @Query("""
            SELECT new org.tutorbooking.dto.response.TopTutorResponse(
                t.id, u.fullName, u.avatarUrl, u.email, AVG(r.rating), COUNT(r.id)
            )
            FROM Tutor t
            JOIN t.user u
            JOIN Review r ON r.tutor.id = t.id
            WHERE t.approvalStatus = 'approved'
            GROUP BY t.id, u.fullName, u.avatarUrl, u.email
            ORDER BY AVG(r.rating) DESC
            """)
    Page<TopTutorResponse> findTopTutors(Pageable pageable);
}
