package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tutorbooking.domain.entity.TutorAvailability;

import java.util.List;

@Repository
public interface TutorAvailabilityRepository extends JpaRepository<TutorAvailability, Long> {
    List<TutorAvailability> findByTutorId(Long tutorId);
    List<TutorAvailability> findByTutorIdAndIsActiveTrue(Long tutorId);
}
