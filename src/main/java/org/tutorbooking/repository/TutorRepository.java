package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.tutorbooking.domain.entity.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByUserId(Long userId);

    // khách hàng xem profile của gia sư (có user)
    @Query("""
                SELECT t FROM Tutor t
                JOIN FETCH t.user
                WHERE t.id = :id
            """)
    Optional<Tutor> findDetailById(@Param("id") Long id);

    //  gia sư tụ xem profile của mình (có user)
    @Query("""
                SELECT t FROM Tutor t
                JOIN FETCH t.user
                WHERE t.user.id = :userId
            """)
    Optional<Tutor> findByUserIdWithUser(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    @Query(value = """
            SELECT DISTINCT t FROM Tutor t 
            JOIN FETCH t.user u 
            LEFT JOIN TutorSubject ts ON ts.tutor = t 
            WHERE t.approvalStatus = 'approved' 
              AND (:subjectId IS NULL OR ts.subject.id = :subjectId) 
              AND (:grade IS NULL OR ts.gradeLevel = :grade) 
              AND (:minPrice IS NULL OR ts.pricePerSession >= :minPrice) 
              AND (:maxPrice IS NULL OR ts.pricePerSession <= :maxPrice) 
              AND (:teachingMode IS NULL OR t.teachingMode = :teachingMode)
            """,
            countQuery = """
            SELECT COUNT(DISTINCT t) FROM Tutor t 
            LEFT JOIN TutorSubject ts ON ts.tutor = t
            WHERE t.approvalStatus = 'approved' 
              AND (:subjectId IS NULL OR ts.subject.id = :subjectId) 
              AND (:grade IS NULL OR ts.gradeLevel = :grade) 
              AND (:minPrice IS NULL OR ts.pricePerSession >= :minPrice) 
              AND (:maxPrice IS NULL OR ts.pricePerSession <= :maxPrice) 
              AND (:teachingMode IS NULL OR t.teachingMode = :teachingMode)
            """)
    Page<Tutor> searchApprovedTutors(
            @Param("subjectId") Long subjectId,
            @Param("grade") Integer grade,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("teachingMode") String teachingMode,
            Pageable pageable
    );
}

