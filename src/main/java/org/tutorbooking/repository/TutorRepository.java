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

    // gia sư tụ xem profile của mình (có user)
    @Query("""
                SELECT t FROM Tutor t
                JOIN FETCH t.user
                WHERE t.user.id = :userId
            """)
    Optional<Tutor> findByUserIdWithUser(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

}
